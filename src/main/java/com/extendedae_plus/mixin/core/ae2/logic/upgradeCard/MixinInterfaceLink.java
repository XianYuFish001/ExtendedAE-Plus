package com.extendedae_plus.mixin.core.ae2.logic.upgradeCard;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import com.extendedae_plus.common.wireless.HolderLinkChannelCard;
import com.extendedae_plus.mixin.helper.HelperHolderCardLink;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InterfaceLogic.class)
public class MixinInterfaceLink implements HelperHolderCardLink {
    @Shadow
    @Final
    protected IManagedGridNode mainNode;
    @Mutable
    @Shadow
    @Final
    private IUpgradeInventory upgrades;
    @Unique
    private HolderLinkChannelCard eaep$linkLogic = HolderLinkChannelCard.Empty;

    @Shadow
    private void onUpgradesChanged() {}

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/InterfaceLogicHost;Lnet/minecraft/world/item/Item;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode gridNode, InterfaceLogicHost host, Item is, int slots, CallbackInfo ci) {
        this.eaep$linkLogic = new HolderLinkChannelCard(this.mainNode,
                host::getBlockEntity, host::getUpgrades);
        this.upgrades = UpgradeInventories.forMachine(is, Math.min(this.upgrades.size() + 2, 8), this::onUpgradesChanged);
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

    @Inject(method = "onUpgradesChanged", at = @At("HEAD"))
    private void onUpgradesChanged(CallbackInfo ci) {
        this.eaep$linkLogic.onUpgradesChanged();
    }

    @Mixin(targets = "appeng.helpers.InterfaceLogic$Ticker")
    private static class MixinProviderTicker {
        @Shadow
        @Final
        InterfaceLogic this$0;

        @Inject(method = "tickingRequest", at = @At("HEAD"))
        private void onTicking(IGridNode node, int ticksSinceLastCall, CallbackInfoReturnable<TickRateModulation> cir) {
            if (!(this$0 instanceof HelperHolderCardLink helper)) return;
            var logic = helper.eaep$holder();

            if (!logic.needsInitialize()) return;
            logic.onTickingInitialize();
        }
    }
}
