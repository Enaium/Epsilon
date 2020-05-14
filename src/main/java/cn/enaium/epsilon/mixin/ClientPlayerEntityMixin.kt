package cn.enaium.epsilon.mixin

import cn.enaium.epsilon.Epsilon
import cn.enaium.epsilon.event.Event
import cn.enaium.epsilon.event.events.EventMotion
import cn.enaium.epsilon.event.events.EventUpdate
import net.minecraft.client.network.ClientPlayerEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * Project: Epsilon
 * -----------------------------------------------------------
 * Copyright © 2020 | Enaium | All rights reserved.
 */
@Mixin(ClientPlayerEntity::class)
class ClientPlayerEntityMixin {
    @Inject(at = [At("HEAD")], method = ["sendChatMessage"], cancellable = true)
    private fun onSendChatMessage(message: String, info: CallbackInfo) {
        if (Epsilon.commandManager.processCommand(message)) {
            info.cancel()
        }
    }

    @Inject(at = [At("HEAD")], method = ["tick()V"])
    private fun preTick(ci: CallbackInfo) {
        EventUpdate().call()
    }

    @Inject(at = [At("HEAD")], method = ["sendMovementPackets()V"])
    private fun onSendMovementPacketsHEAD(ci: CallbackInfo) {
        EventMotion(Event.Type.PRE).call()
    }

    @Inject(at = [At("TAIL")], method = ["sendMovementPackets()V"])
    private fun onSendMovementPacketsTAIL(ci: CallbackInfo) {
        EventMotion(Event.Type.POST).call()
    }
}