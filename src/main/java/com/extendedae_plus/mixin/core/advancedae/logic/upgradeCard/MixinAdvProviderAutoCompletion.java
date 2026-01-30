package com.extendedae_plus.mixin.core.advancedae.logic.upgradeCard;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.KeyCounter;
import com.extendedae_plus.mixin.bridge.HelperProviderUpgradesInv;
import com.extendedae_plus.mixin.impl.HolderCardAutoCompletionState;
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
public class MixinAdvProviderAutoCompletion {
    @Shadow
    @Final
    private IManagedGridNode mainNode;
    @Unique
    private HolderCardAutoCompletionState eaep$cardLogic = HolderCardAutoCompletionState.EMPTY;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;I)V",
            at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, AdvPatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        this.eaep$cardLogic = new HolderCardAutoCompletionState(
                this.mainNode, ((HelperProviderUpgradesInv) this)::eaep$getUpgradeInventory);
        ((HelperProviderUpgradesInv) this).eaep$addAction(this.eaep$cardLogic::onUpgradesChanged);
    }

    @Inject(method = "pushPattern", at = @At("RETURN"))
    private void onPush(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        this.eaep$cardLogic.completeJob(patternDetails);
    }
}
