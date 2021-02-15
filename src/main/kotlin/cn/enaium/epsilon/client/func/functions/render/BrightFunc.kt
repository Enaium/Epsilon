package cn.enaium.epsilon.client.func.functions.render

import cn.enaium.cf4m.annotation.Event
import cn.enaium.cf4m.annotation.Setting
import cn.enaium.cf4m.annotation.module.Disable
import cn.enaium.cf4m.annotation.module.Module
import cn.enaium.cf4m.module.Category
import cn.enaium.cf4m.setting.settings.ModeSetting
import cn.enaium.epsilon.client.MC
import cn.enaium.epsilon.client.events.Render2DEvent
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects


/**
 * Project: Epsilon
 * License: GPL-3.0
 * -----------------------------------------------------------
 * Copyright © 2020-2021 | Enaium | All rights reserved.
 */
@Module("Bright", category = Category.RENDER)
class BrightFunc {

    @Setting
    private val mode = ModeSetting(this, "Mode", "Mode", "Gamma", arrayListOf("Gamma", "NightVision"))

    @Event
    fun on(render2DEvent: Render2DEvent) {
        when (mode.current) {
            "Gamma" -> MC.options.gamma = 300.0
            "NightVision" -> MC.player!!.addStatusEffect(
                StatusEffectInstance(
                    StatusEffects.NIGHT_VISION,
                    16360,
                    0,
                    false,
                    false
                )
            )
        }
    }

    @Disable
    fun onDisable() {
        MC.options.gamma = 1.0
        MC.player!!.removeStatusEffectInternal(StatusEffects.NIGHT_VISION)
    }
}