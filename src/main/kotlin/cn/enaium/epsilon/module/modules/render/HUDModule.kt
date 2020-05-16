package cn.enaium.epsilon.module.modules.render

import cn.enaium.epsilon.Epsilon
import cn.enaium.epsilon.Epsilon.MC
import cn.enaium.epsilon.event.EventAT
import cn.enaium.epsilon.event.events.EventKeyboard
import cn.enaium.epsilon.event.events.EventRender2D
import cn.enaium.epsilon.module.Category
import cn.enaium.epsilon.module.Module
import cn.enaium.epsilon.module.ModuleAT
import cn.enaium.epsilon.setting.Setting
import cn.enaium.epsilon.setting.SettingAT
import cn.enaium.epsilon.setting.settings.*
import cn.enaium.epsilon.utils.ColorUtils
import cn.enaium.epsilon.utils.FontUtils
import cn.enaium.epsilon.utils.FontUtils.drawStringWithShadow
import cn.enaium.epsilon.utils.FontUtils.fontHeight
import cn.enaium.epsilon.utils.FontUtils.getWidth
import cn.enaium.epsilon.utils.Render2DUtils
import cn.enaium.epsilon.utils.Render2DUtils.scaledHeight
import cn.enaium.epsilon.utils.Render2DUtils.scaledWidth
import cn.enaium.epsilon.utils.Utils
import net.minecraft.entity.Entity
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList


/**
 * Project: Epsilon
 * -----------------------------------------------------------
 * Copyright © 2020 | Enaium | All rights reserved.
 */
@ModuleAT
class HUDModule : Module("HUD", GLFW.GLFW_KEY_P, Category.RENDER) {
    private var categoryValues: ArrayList<Category> = ArrayList()
    private var currentCategoryIndex = 0
    private var currentModIndex = 0
    private var currentSettingIndex = 0

    private var editMode = false

    private var screen = 0

    @SettingAT
    private val tabGUI = EnableSetting(this, "TabGUI", true)

    @SettingAT
    private val list = EnableSetting(this, "List", true)

    @SettingAT
    private val entityList = EnableSetting(this, "EntityList", false)

    init {
        categoryValues = ArrayList()
        currentCategoryIndex = 0
        currentModIndex = 0
        currentSettingIndex = 0
        editMode = false
        screen = 0
        this.categoryValues.addAll(Category.values());
    }

    @EventAT
    fun entityList(eventRender2D: EventRender2D) {
        if (!entityList.enable)
            return

        val entities: MutableSet<String> = HashSet()
        for (e in MC.world!!.entities) {
            entities.add(e.javaClass.simpleName)
        }

        entities.sortedWith(Comparator { o1: String, o2: String -> getWidth(o2) - getWidth(o1) })

        var yStart = scaledHeight - fontHeight

        for (entity in entities) {
            val startX = scaledWidth - getWidth(entity) - 6
            drawStringWithShadow(eventRender2D.matrixStack, entity, startX + 3, yStart, Color.WHITE.rgb)
            yStart -= fontHeight + 4
        }
    }

    @EventAT
    fun list(eventRender2D: EventRender2D) {
        if (!list.enable)
            return

        drawStringWithShadow(eventRender2D.matrixStack, "" + Formatting.WHITE + Epsilon.NAME + Formatting.RESET
                + Epsilon.VERSION, 5, 5, Color(67, 0, 99).rgb)

        var yStart = 1

        val modules = ArrayList<Module>()
        for (m in Epsilon.moduleManager.modules) {
            if (m.enable) {
                modules.add(m)
            }
        }

        val mods: ArrayList<Module> = modules
        mods.sortByDescending { getWidth(it.getDisplayTag()) }
        for (module in mods) {
            val startX = scaledWidth - getWidth(module.getDisplayTag()) - 6
            drawStringWithShadow(eventRender2D.matrixStack, module.name, startX + 3, yStart, Color.WHITE.rgb)
            yStart += fontHeight + 4
        }
    }

    @EventAT
    fun tabGUI(eventRender2D: EventRender2D) {
        if (!tabGUI.enable)
            return

        val startX = 5
        var startY = 5 + 9 + 2
        Render2DUtils.drawRect(eventRender2D.matrixStack, startX, startY, startX + this.getWidestCategory() + 5, startY + categoryValues.size * (fontHeight + 2), ColorUtils.BG)
        for (c in categoryValues) {
            if (getCurrentCategory() == c) {
                Render2DUtils.drawRect(eventRender2D.matrixStack, startX + 1, startY, startX + this.getWidestCategory() + 5 - 1, startY + fontHeight + 2, ColorUtils.SELECT)
            }
            val name: String = c.name
            FontUtils.drawStringWithShadow(eventRender2D.matrixStack, name.substring(0, 1).toUpperCase() + name.substring(1, name.length).toLowerCase(), startX + 2 + if (getCurrentCategory() == c) 2 else 0, startY + 2, -1)
            startY += fontHeight + 2
        }

        if (screen == 1 || screen == 2) {
            val startModsX: Int = startX + this.getWidestCategory() + 6
            var startModsY = 5 + 9 + 2 + currentCategoryIndex * (fontHeight + 2)
            Render2DUtils.drawRect(eventRender2D.matrixStack, startModsX, startModsY, startModsX + this.getWidestMod() + 5, startModsY + getModsForCurrentCategory().size * (fontHeight + 2), ColorUtils.BG)
            for (m in getModsForCurrentCategory()) {
                if (getCurrentModule() == m) {
                    Render2DUtils.drawRect(eventRender2D.matrixStack, startModsX + 1, startModsY, startModsX + this.getWidestMod() + 5 - 1, startModsY + fontHeight + 2, ColorUtils.SELECT)
                }
                FontUtils.drawStringWithShadow(eventRender2D.matrixStack, m.name + if (Epsilon.settingManager.getSettingsForModule(m) != null) ">" else "", startModsX + 2 + if (getCurrentModule() == m) 2 else 0, startModsY + 2, if (m.enable) -1 else Color.GRAY.rgb)
                startModsY += fontHeight + 2
            }
        }
        if (screen == 2) {
            val startSettingX: Int = startX + getWidestCategory() + 6 + getWidestCategory() + 8
            var startSettingY = 5 + 9 + 2 + currentCategoryIndex * (9 + 2) + currentModIndex * (9 + 2)
            Render2DUtils.drawRect(eventRender2D.matrixStack, startSettingX, startSettingY, startSettingX + getWidestSetting() + 5, startSettingY + getSettingForCurrentMod()!!.size * (fontHeight + 2), ColorUtils.BG)
            for (s in getSettingForCurrentMod()!!) {
                if (getCurrentSetting() == s) {
                    Render2DUtils.drawRect(eventRender2D.matrixStack, startSettingX + 1, startSettingY, startSettingX + this.getWidestSetting() + 5 - 1, startSettingY + fontHeight + 2, ColorUtils.SELECT)
                }
                when (s) {
                    is EnableSetting -> {
                        FontUtils.drawStringWithShadow(eventRender2D.matrixStack, s.name + ": " + s.enable, startSettingX + 2 + if (getCurrentSetting() == s) 2 else 0, startSettingY + 2, if (editMode && getCurrentSetting() == s) -1 else Color.GRAY.rgb)
                    }
                    is IntegerSetting -> {
                        FontUtils.drawStringWithShadow(eventRender2D.matrixStack, s.name + ": " + s.current, startSettingX + 2 + if (getCurrentSetting() == s) 2 else 0, startSettingY + 2, if (editMode && getCurrentSetting() == s) -1 else Color.GRAY.rgb)
                    }
                    is DoubleSetting -> {
                        FontUtils.drawStringWithShadow(eventRender2D.matrixStack, s.name + ": " + s.current, startSettingX + 2 + if (getCurrentSetting() == s) 2 else 0, startSettingY + 2, if (editMode && getCurrentSetting() == s) -1 else Color.GRAY.rgb)
                    }
                    is FloatSetting -> {
                        FontUtils.drawStringWithShadow(eventRender2D.matrixStack, s.name + ": " + s.current, startSettingX + 2 + if (getCurrentSetting() == s) 2 else 0, startSettingY + 2, if (editMode && getCurrentSetting() == s) -1 else Color.GRAY.rgb)
                    }
                    is LongSetting -> {
                        FontUtils.drawStringWithShadow(eventRender2D.matrixStack, s.name + ": " + s.current, startSettingX + 2 + if (getCurrentSetting() == s) 2 else 0, startSettingY + 2, if (editMode && getCurrentSetting() == s) -1 else Color.GRAY.rgb)
                    }
                    is ModeSetting -> {
                        FontUtils.drawStringWithShadow(eventRender2D.matrixStack, s.name + ": " + s.current, startSettingX + 2 + if (getCurrentSetting() == s) 2 else 0, startSettingY + 2, if (editMode && getCurrentSetting() == s) -1 else Color.GRAY.rgb)
                    }
                }
                startSettingY += fontHeight + 2
            }
        }


    }

    private fun up() {
        if (currentCategoryIndex > 0 && screen == 0) {
            currentCategoryIndex--
        } else if (currentCategoryIndex == 0 && screen == 0) {
            currentCategoryIndex = categoryValues.size - 1
        } else if (currentModIndex > 0 && screen == 1) {
            currentModIndex--
        } else if (currentModIndex == 0 && screen == 1) {
            currentModIndex = this.getModsForCurrentCategory().size - 1
        } else if (currentSettingIndex > 0 && screen == 2 && !editMode) {
            currentSettingIndex--
        } else if (currentSettingIndex == 0 && screen == 2 && !editMode) {
            currentSettingIndex = this.getSettingForCurrentMod()!!.size - 1
        }
        if (editMode) {
            val s: Setting = this.getCurrentSetting()
            if (s is EnableSetting) {
                s.enable = !s.enable
            } else if (s is IntegerSetting) {
                if (s.current < s.max) s.current = s.current + 1
            } else if (s is DoubleSetting) {
                if (s.current < s.max) s.current = Utils.valueFix(s.current + 0.1)
            } else if (s is FloatSetting) {
                if (s.current < s.max) s.current = Utils.valueFix(s.current + 0.1f)
            } else if (s is LongSetting) {
                if (s.current < s.max) s.current = s.current + 1
            } else {
                try {
                    (s as ModeSetting).current = s.modes[s.getCurrentIndex() - 1]
                } catch (e: Exception) {
                    (s as ModeSetting).current = s.modes[s.modes.size - 1]
                }
            }
        }
    }

    private fun down() {
        if (currentCategoryIndex < categoryValues.size - 1 && screen == 0) {
            currentCategoryIndex++
        } else if (currentCategoryIndex == categoryValues.size - 1 && screen == 0) {
            currentCategoryIndex = 0
        } else if (currentModIndex < this.getModsForCurrentCategory().size - 1 && screen == 1) {
            currentModIndex++
        } else if (currentModIndex == this.getModsForCurrentCategory().size - 1 && screen == 1) {
            currentModIndex = 0
        } else if (currentSettingIndex < this.getSettingForCurrentMod()!!.size - 1 && screen == 2 && !editMode) {
            currentSettingIndex++
        } else if (currentSettingIndex == this.getSettingForCurrentMod()!!.size - 1 && screen == 2 && !editMode) {
            currentSettingIndex = 0
        }
        if (editMode) {
            val s: Setting = this.getCurrentSetting()
            if (s is EnableSetting) {
                s.enable = !s.enable
            } else if (s is IntegerSetting) {
                if (s.current > s.min) s.current = s.current - 1
            } else if (s is DoubleSetting) {
                if (s.current > s.min) s.current = Utils.valueFix(s.current - 0.1)
            } else if (s is FloatSetting) {
                if (s.current > s.min) s.current = Utils.valueFix(s.current - 0.1f)
            } else if (s is LongSetting) {
                if (s.current > s.min) s.current = s.current - 1
            } else {
                try {
                    (s as ModeSetting).current = s.modes[s.getCurrentIndex() + 1]
                } catch (e: Exception) {
                    (s as ModeSetting).current = s.modes[0]
                }
            }
        }
    }

    private fun right(key: Int) {
        if (screen == 0) {
            screen = 1
        } else if (screen == 1 && this.getSettingForCurrentMod() == null) {
            this.getCurrentModule().enable()
        } else if (screen == 1 && this.getSettingForCurrentMod() != null && key == GLFW.GLFW_KEY_ENTER) {
            this.getCurrentModule().enable()
        } else if (screen == 1 && this.getSettingForCurrentMod() != null) {
            screen = 2
        } else if (screen == 2) {
            editMode = !editMode
        }
    }

    private fun left() {
        if (screen == 1) {
            screen = 0
            currentModIndex = 0
        } else if (screen == 2) {
            screen = 1
            currentSettingIndex = 0
        }
    }

    @EventAT
    fun onKey(eventKeyBoard: EventKeyboard) {
        if (MC.currentScreen != null) return
        if (eventKeyBoard.action != GLFW.GLFW_PRESS) return
        when (eventKeyBoard.keyCode) {
            GLFW.GLFW_KEY_UP -> up()
            GLFW.GLFW_KEY_DOWN -> down()
            GLFW.GLFW_KEY_RIGHT -> right(GLFW.GLFW_KEY_RIGHT)
            GLFW.GLFW_KEY_LEFT -> left()
            GLFW.GLFW_KEY_ENTER -> right(GLFW.GLFW_KEY_ENTER)
        }
    }

    private fun getCurrentSetting(): Setting {
        return getSettingForCurrentMod()!![currentSettingIndex]
    }

    private fun getSettingForCurrentMod(): ArrayList<Setting>? {
        return Epsilon.settingManager.getSettingsForModule(getCurrentModule())
    }

    private fun getCurrentCategory(): Category {
        return categoryValues[currentCategoryIndex]
    }

    private fun getCurrentModule(): Module {
        return getModsForCurrentCategory()[currentModIndex]
    }

    private fun getModsForCurrentCategory(): ArrayList<Module> {
        return Epsilon.moduleManager.getModulesForCategory(getCurrentCategory())
    }

    private fun getWidestSetting(): Int {
        var width = 0
        for (s in getSettingForCurrentMod()!!) {
            val name: String = when (s) {
                is EnableSetting -> {
                    s.name + ": " + s.enable
                }
                is IntegerSetting -> {
                    s.name + ": " + s.current
                }
                is FloatSetting -> {
                    s.name + ": " + s.current
                }
                is DoubleSetting -> {
                    s.name + ": " + s.current
                }
                is LongSetting -> {
                    s.name + ": " + s.current
                }
                else -> {
                    s.name + ": " + (s as ModeSetting).current
                }
            }
            if (getWidth(name) > width) {
                width = getWidth(name)
            }
        }
        return width
    }

    private fun getWidestMod(): Int {
        var width = 0
        for (m in Epsilon.moduleManager.modules) {
            val cWidth = getWidth(m.name)
            if (cWidth > width) {
                width = cWidth
            }
        }
        return width
    }

    private fun getWidestCategory(): Int {
        var width = 0
        for (c in categoryValues) {
            val name: String = c.name
            val cWidth = getWidth(name.substring(0, 1).toUpperCase() + name.substring(1, name.length).toLowerCase())
            if (cWidth > width) {
                width = cWidth
            }
        }
        return width
    }


}