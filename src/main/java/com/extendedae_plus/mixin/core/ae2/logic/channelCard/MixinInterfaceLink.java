package com.extendedae_plus.mixin.core.ae2.logic.channelCard;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import com.extendedae_plus.common.wireless.HolderLinkChannelCard;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InterfaceLogic.class)
public class MixinInterfaceLink {
    @Shadow
    @Final
    protected IManagedGridNode mainNode;
    @Mutable
    @Shadow
    @Final
    private IUpgradeInventory upgrades;
    @Unique
    private HolderLinkChannelCard eaep$linkLogic = HolderLinkChannelCard.EMPTY;

    @Shadow
    private void onUpgradesChanged() {}

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/InterfaceLogicHost;Lnet/minecraft/world/item/Item;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode gridNode, InterfaceLogicHost host, Item is, int slots, CallbackInfo ci) {
        this.eaep$linkLogic = new HolderLinkChannelCard(mainNode,
                host::getBlockEntity, host::getUpgrades);
        this.upgrades = UpgradeInventories.forMachine(is, Math.min(this.upgrades.size()+ 1, 8), this::onUpgradesChanged);
    }

    @Inject(method = "hasWorkToDo", at = @At("TAIL"), cancellable = true)
    private void testAdditionalWork(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        cir.setReturnValue(this.eaep$linkNeedsInitialize());
    }

    @Inject(method = "updateStorage", at = @At("HEAD"))
    private void doAdditionalWork(CallbackInfoReturnable<Boolean> cir) {
        this.eaep$linkLogic.onTickingInitialize();
    }

    @Inject(method = "onUpgradesChanged", at = @At("HEAD"))
    private void onUpgradesChanged(CallbackInfo ci) {
        this.eaep$linkLogic.onUpgradesChanged();
    }

    @Unique
    public boolean eaep$linkNeedsInitialize() {
        return this.eaep$linkLogic.needsInitialize();
    }

    @Mixin(targets = "appeng.helpers.InterfaceLogic$Ticker")
    private static class MixinProviderTicker {
        @Shadow
        @Final
        InterfaceLogic this$0;

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
