package com.extendedae_plus.mixin.core.ae2.parts.upgradeCard;

import appeng.api.parts.IPartItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.parts.automation.UpgradeablePart;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.wireless.HolderLinkChannelCard;
import com.extendedae_plus.mixin.bridge.HelperPartLinkLogic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UpgradeablePart.class)
public abstract class MixinUpgradeablePartsCardSlot implements HelperPartLinkLogic {
    @Unique
    private HolderLinkChannelCard eaep$linkLogic = HolderLinkChannelCard.EMPTY;
    @Unique
    private boolean eaep$supportedChannelCard = false;

    @Mutable
    @Shadow
    @Final
    private IUpgradeInventory upgrades;
    @Shadow
    protected abstract void onUpgradesChanged();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(IPartItem<?> partItem, CallbackInfo ci) {
        this.eaep$supportedChannelCard =
                Upgrades.getMaxInstallable(ModItems.CHANNEL_CARD.get(), partItem) > 0;

        if (!this.eaep$supportedChannelCard) return;
        var self = (UpgradeablePart)(Object) this;

        this.eaep$linkLogic = new HolderLinkChannelCard(self.getMainNode(),
                self::getBlockEntity, self::getUpgrades);
        this.upgrades = UpgradeInventories.forMachine(
                partItem.asItem(), Math.min(this.upgrades.size() + 1, 8), this::onUpgradesChanged);
    }

    @Inject(method = "onUpgradesChanged", at = @At("HEAD"))
    private void onUpgradesChanged(CallbackInfo ci) {
        if (this.eaep$supportedChannelCard) this.eaep$linkLogic.onUpgradesChanged();
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void onReadingComponents(CompoundTag extra, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.eaep$supportedChannelCard) this.eaep$linkLogic.onTickingInitialize();
    }

    @Unique
    public void eaep$updateLinkStatus() {
        if (this.eaep$needsLinkUpdate() && this.eaep$supportedChannelCard) {
            this.eaep$linkLogic.onTickingInitialize();
        }
    }

    @Unique
    public boolean eaep$needsLinkUpdate() {
        return this.eaep$supportedChannelCard && this.eaep$linkLogic.needsInitialize();
    }
}
