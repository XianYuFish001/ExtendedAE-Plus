package com.extendedae_plus.mixin.core.ae2.logic.upgradeCard;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.extendedae_plus.common.wireless.HolderLinkChannelCard;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderUpgradesInv;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternProviderLogic.class)
public class MixinProviderLink {
    @Shadow
    @Final
    private IManagedGridNode mainNode;
    @Unique
    private HolderLinkChannelCard eaep$linkLogic = HolderLinkChannelCard.EMPTY;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        this.eaep$linkLogic = new HolderLinkChannelCard(this.mainNode,
                host::getBlockEntity,
                ((HelperProviderUpgradesInv) this)::eaep$getUpgradeInventory);
        ((HelperProviderUpgradesInv) this).eaep$addAction(this.eaep$linkLogic::onUpgradesChanged);
    }

    @Inject(method = "hasWorkToDo", at = @At("TAIL"), cancellable = true)
    private void testAdditionalWork(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        cir.setReturnValue(this.eaep$linkNeedsInitialize());
    }

    @Inject(method = "doWork", at = @At("HEAD"))
    private void doAdditionalWork(CallbackInfoReturnable<Boolean> cir) {
        this.eaep$linkLogic.onTickingInitialize();
    }

    @Unique
    public boolean eaep$linkNeedsInitialize() {
        return this.eaep$linkLogic.needsInitialize();
    }

    @Mixin(targets = "appeng.helpers.patternprovider.PatternProviderLogic$Ticker")
    private static class MixinProviderTicker {
        @Shadow
        @Final
        PatternProviderLogic this$0;

        @Inject(method = "tickingRequest", at = @At("HEAD"))
        private void onTicking(IGridNode node, int ticksSinceLastCall, CallbackInfoReturnable<TickRateModulation> cir) {
            try {
                if ((boolean) this$0.getClass()
                        .getMethod("eaep$linkNeedsInitialize")
                        .invoke(this$0)) {
                    var methodDoWork = this$0.getClass().getDeclaredMethod("doWork");
                    methodDoWork.setAccessible(true);
                    methodDoWork.invoke(this$0);
                }
            } catch (Throwable ignore) {
            }
        }
    }
}
