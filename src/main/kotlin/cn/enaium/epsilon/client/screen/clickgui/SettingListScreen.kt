package cn.enaium.epsilon.client.screen.clickgui

import cn.enaium.epsilon.client.MC
import cn.enaium.epsilon.client.cf4m
import cn.enaium.epsilon.client.settings.*
import cn.enaium.epsilon.client.ui.UI
import cn.enaium.epsilon.client.ui.elements.*
import cn.enaium.epsilon.client.utils.Render2DUtils
import org.lwjgl.glfw.GLFW

/**
 * Project: Epsilon
 * License: GPL-3.0
 * -----------------------------------------------------------
 * Copyright © 2020-2021 | Enaium | All rights reserved.
 */
class SettingListScreen(val func: Any) : UI() {

    override fun initUI() {
        super.initUI()
        var y = 0
        val scrollPanel = ScrollPanel(Render2DUtils.scaledWidth / 2 - 50, 50, 100, 120)
        val settings = cf4m.setting.getSettings(func)
        if (settings != null) {
            for (setting in settings) {
                if (setting is EnableSetting) {
                    scrollPanel.addElement(object :
                        CheckBox(0, y, cf4m.setting.getName(func, setting), setting.enable) {
                        override fun onLeftClicked() {
                            setting.enable = this.checked
                            super.onLeftClicked()
                        }
                    })
                } else if (setting is IntegerSetting || setting is FloatSetting || setting is DoubleSetting || setting is LongSetting) {
                    val textField = TextField(0, y, 50)
                    textField.setSuggestion(cf4m.setting.getName(func, setting))
                    when (setting) {
                        is IntegerSetting -> {
                            textField.setText(setting.current.toString())
                            scrollPanel.addElement(object : Button(60, y, 40, 20, "SET") {
                                override fun onLeftClicked() {
                                    setting.current = textField.getText().toInt()
                                    super.onLeftClicked()
                                }
                            })
                        }
                        is FloatSetting -> {
                            textField.setText(setting.current.toString())
                            scrollPanel.addElement(object : Button(60, y, 40, 20, "SET") {
                                override fun onLeftClicked() {
                                    setting.current = textField.getText().toFloat()
                                    super.onLeftClicked()
                                }
                            })
                        }
                        is DoubleSetting -> {
                            textField.setText(setting.current.toString())
                            scrollPanel.addElement(object : Button(60, y, 40, 20, "SET") {
                                override fun onLeftClicked() {
                                    setting.current = textField.getText().toDouble()
                                    super.onLeftClicked()
                                }
                            })
                        }
                        is LongSetting -> {
                            textField.setText(setting.current.toString())
                            scrollPanel.addElement(object : Button(60, y, 40, 20, "SET") {
                                override fun onLeftClicked() {
                                    setting.current = textField.getText().toLong()
                                    super.onLeftClicked()
                                }
                            })
                        }
                    }
                    scrollPanel.addElement(textField)
                } else if (setting is ModeSetting) {
                    scrollPanel.addElement(object :
                        ModeButton(0, y, setting.modes as ArrayList<String>, getCurrentModeIndex(setting)) {
                        override fun onLeftClicked() {
                            try {
                                setting.current = setting.modes[getCurrentModeIndex(setting) + 1]
                            } catch (e: Exception) {
                                setting.current = setting.modes.first()
                            }
                            this.current = getCurrentModeIndex(setting)
                            super.onLeftClicked()
                        }

                        override fun onRightClicked() {
                            try {
                                setting.current = setting.modes[getCurrentModeIndex(setting) - 1]
                            } catch (e: Exception) {
                                setting.current = setting.modes.last()
                            }
                            this.current = getCurrentModeIndex(setting)
                            super.onRightClicked()
                        }
                    })
                } else if (setting is BlockListSetting) {
                    scrollPanel.addElement(object : Button(0, y, "Set:${cf4m.setting.getName(func, setting)}") {
                        override fun onLeftClicked() {
                            MC.openScreen(EditBlockListSettingScreen(setting))
                            super.onLeftClicked()
                        }
                    })
                }
                y += 30
            }
        }
        addElement(scrollPanel)
    }

    private fun getCurrentModeIndex(modeSetting: ModeSetting): Int {
        var index = 0
        for (ms in modeSetting.modes) {
            index++
            if (modeSetting.current.equals(ms)) {
                return index
            }
        }
        return 0;
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
            MC.openScreen(CategoryListScreen())
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }
}