package com.extendedae_plus.mixin.core.ae2.logic.upgradeCard;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.extendedae_plus.common.wireless.HolderLinkChannelCard;
import com.extendedae_plus.mixin.helper.HelperHolderCardLink;
import com.extendedae_plus.mixin.helper.HelperProviderUpgradesInv;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternProviderLogic.class)
public class MixinProviderLink implements HelperHolderCardLink {
    @Shadow
    @Final
    private IManagedGridNode mainNode;
    @Unique
    private HolderLinkChannelCard eaep$linkLogic = HolderLinkChannelCard.Empty;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        this.eaep$linkLogic = new HolderLinkChannelCard(this.mainNode,
                host::getBlockEntity,
                ((HelperProviderUpgradesInv) this)::eaep$getUpgradeInventory);
        ((HelperProviderUpgradesInv) this).eaep$addAction(this.eaep$linkLogic::onUpgradesChanged);
    }

    @Override
    public @NotNull HolderLinkChannelCard eaep$holder() {
        return this.eaep$linkLogic;
    }

    @Inject(method = "hasWorkToDo", at = @At("TAIL"), cancellable = true)
    private void testAdditionalWork(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        cir.setReturnValue(this.eaep$linkLogic.needsInitialize());
    }

    @Mixin(targets = "appeng.helpers.patternprovider.PatternProviderLogic$Ticker")
    private static class MixinProviderTicker {
        @Shadow
        @Final
        PatternProviderLogic this$0;

        @Inject(method = "tickingRequest", at = @At("HEAD"))
        private void onTicking(IGridNode node, int ticksSinceLastCall, CallbackInfoReturnable<TickRateModulation> cir) {
            if (!(this$0 instanceof HelperHolderCardLink helper)) return;
            var logic = helper.eaep$holder();

            if (!logic.needsInitialize()) return;
            logic.onTickingInitialize();
        }
    }
}
