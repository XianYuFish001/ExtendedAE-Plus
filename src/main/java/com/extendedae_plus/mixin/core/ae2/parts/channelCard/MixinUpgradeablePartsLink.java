package com.extendedae_plus.mixin.core.ae2.parts.channelCard;

import appeng.api.parts.IPartItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.parts.automation.UpgradeablePart;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.mixin.impl.HolderChannelCardLink;
import com.extendedae_plus.mixin.impl.bridge.HelperPartLinkLogic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UpgradeablePart.class)
public abstract class MixinUpgradeablePartsLink implements HelperPartLinkLogic {
    @Unique
    private HolderChannelCardLink eaep$linkLogic = HolderChannelCardLink.EMPTY;
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

        this.eaep$linkLogic = new HolderChannelCardLink(self.getMainNode(),
                self::getBlockEntity, self::getUpgrades, () -> self.getHost().markForSave());
        this.upgrades = UpgradeInventories.forMachine(
                partItem.asItem(), Math.max(this.upgrades.size() + 1, 8), this::onUpgradesChanged);
    }

    @Inject(method = "onUpgradesChanged", at = @At("HEAD"))
    private void onUpgradesChanged(CallbackInfo ci) {
        if (this.eaep$supportedChannelCard) this.eaep$linkLogic.updateLinkStatus();
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void onReadingComponents(CompoundTag extra, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.eaep$supportedChannelCard) this.eaep$linkLogic.updateLinkStatus();
    }

    @Unique
    public void eaep$updateLinkStatus() {
        if (this.eaep$needsLinkUpdate() && this.eaep$supportedChannelCard) {
            this.eaep$linkLogic.updateLinkStatus();
        }
    }

    @Unique
    public boolean eaep$needsLinkUpdate() {
        return this.eaep$supportedChannelCard && this.eaep$linkLogic.needsInitialize();
    }
}
