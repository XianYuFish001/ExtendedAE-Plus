package com.extendedae_plus.mixin.core.ae2.parts.upgradeCard;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.parts.automation.IOBusPart;
import com.extendedae_plus.mixin.impl.bridge.HelperPartLinkLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IOBusPart.class)
public class MixinIOBusLinkTicker {
    @Inject(method = "canDoBusWork", at = @At("TAIL"), cancellable = true)
    private void testAdditionalWork(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;

        if (this instanceof HelperPartLinkLogic helper)
            cir.setReturnValue(helper.eaep$needsLinkUpdate());
    }

    @Inject(method = "tickingRequest", at = @At("HEAD"))
    private void onTicking(IGridNode node, int ticksSinceLastCall, CallbackInfoReturnable<TickRateModulation> cir) {
        if (!(this instanceof HelperPartLinkLogic helper)) return;
        helper.eaep$updateLinkStatus();
    }
}
