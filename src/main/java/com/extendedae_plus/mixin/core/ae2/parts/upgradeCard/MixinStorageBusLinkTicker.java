package com.extendedae_plus.mixin.core.ae2.parts.upgradeCard;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.parts.storagebus.StorageBusPart;
import com.extendedae_plus.mixin.bridge.HelperPartLinkLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StorageBusPart.class)
public class MixinStorageBusLinkTicker {
    @Inject(method = "tickingRequest", at = @At("HEAD"))
    private void onTicking(IGridNode node, int ticksSinceLastCall, CallbackInfoReturnable<TickRateModulation> cir) {
        if (!(this instanceof HelperPartLinkLogic helper)) return;
        helper.eaep$updateLinkStatus();
    }
}
