package com.extendedae_plus.mixin.core.ae2.logic.upgradeCard;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.extendedae_plus.mixin.bridge.HelperProviderUpgradesInv;
import com.extendedae_plus.mixin.impl.HelperAutoCompletion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternProviderLogic.class)
public class MixinProviderAutoCompletion {
    @Shadow
    @Final
    private IManagedGridNode mainNode;
    @Unique
    private HelperAutoCompletion eaep$cardLogic = HelperAutoCompletion.EMPTY;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        this.eaep$cardLogic = new HelperAutoCompletion(
                this.mainNode,
                ((HelperProviderUpgradesInv) this)::eaep$getUpgradeInventory,
                host::getBlockEntity
        );
        ((HelperProviderUpgradesInv) this).eaep$addAction(this.eaep$cardLogic::onUpgradesChanged);
    }

    @Inject(method = "pushPattern", at = @At("RETURN"))
    private void onPush(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        this.eaep$cardLogic.completeJob(patternDetails);
    }
}
