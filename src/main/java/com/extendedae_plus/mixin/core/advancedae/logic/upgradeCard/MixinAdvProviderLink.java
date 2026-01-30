package com.extendedae_plus.mixin.core.advancedae.logic.upgradeCard;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import com.extendedae_plus.common.wireless.HolderLinkChannelCard;
import com.extendedae_plus.mixin.bridge.HelperProviderUpgradesInv;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvPatternProviderLogic.class)
public class MixinAdvProviderLink {
    @Shadow
    @Final
    private IManagedGridNode mainNode;
    @Unique
    private HolderLinkChannelCard eaep$linkLogic = HolderLinkChannelCard.EMPTY;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, AdvPatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
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

    @Mixin(targets = "net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic$Ticker")
    private static class MixinAdvProviderTicker {
        @Shadow
        @Final
        AdvPatternProviderLogic this$0;

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
