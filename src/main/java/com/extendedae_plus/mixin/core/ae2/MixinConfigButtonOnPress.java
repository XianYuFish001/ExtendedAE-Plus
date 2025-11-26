package com.extendedae_plus.mixin.core.ae2;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.core.network.serverbound.ConfigButtonPacket;
import com.extendedae_plus.network.CPacketToggleSmartBlocking;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfigButtonPacket.class)
public class MixinConfigButtonOnPress {
    @Shadow
    @Final
    private Setting<?> option;

    @Inject(method = "handleOnServer", at = @At("TAIL"))
    private void onHandlingPacket(ServerPlayer player, CallbackInfo ci) {
        if (!this.option.equals(Settings.BLOCKING_MODE)) return;
        CPacketToggleSmartBlocking.toggleSettings(player.containerMenu, (helper, configManager) -> {
            if (configManager.getSetting(this.option).equals(YesNo.YES)) {
                helper.eaep$setBlockingState(false);
            } else {
                helper.eaep$setBlockingState(false);
                helper.eaep$disableBlocking();
            }
        });
    }
}
