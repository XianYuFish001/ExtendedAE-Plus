package com.extendedae_plus.mixin.core.ae2.logic.channelCard;

import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderUpgradesInv;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.StreamSupport;

@MixinDependencies(conflict = "appflux")
@Mixin(PatternProviderLogic.class)
public class MixinProviderLinkNormal implements HelperProviderUpgradesInv {
    @Unique
    private static final Logger eaep$LOGGER = LogUtils.getLogger();
    @Unique
    private IUpgradeInventory eaep$upgradeInventory = UpgradeInventories.empty();
    @Unique
    private Runnable eaep$onUpgradesChanged;

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        this.eaep$upgradeInventory = UpgradeInventories.forMachine(
                host.getTerminalIcon().getItem(), 1, this::eaep$onUpgradesChanged);
    }

    @Unique
    private void eaep$onUpgradesChanged() {
        try {
            this.eaep$onUpgradesChanged.run();
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] PatternProvider 初始化频道链接失败", throwable);
        }
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void onWritingComponents(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.eaep$upgradeInventory.writeToNBT(tag, "upgrades", registries);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void onReadingComponents(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        try {
            this.eaep$upgradeInventory.readFromNBT(tag, "upgrades", registries);
            this.eaep$onUpgradesChanged.run();
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] PatternProvider 初始化频道链接失败", throwable);
        }
    }

    @Inject(method = "addDrops", at = @At("TAIL"))
    private void onDrop(List<ItemStack> drops, CallbackInfo ci) {
        StreamSupport.stream(this.eaep$upgradeInventory.spliterator(), true)
                .filter(stack -> !stack.isEmpty())
                .forEach(drops::add);
    }

    @Inject(method = "clearContent", at = @At("TAIL"))
    private void onClear(CallbackInfo ci) {
        this.eaep$upgradeInventory.clear();
    }

    @Override
    public void eaep$bindAction(Runnable action) {
        this.eaep$onUpgradesChanged = action;
    }

    @Override
    public IUpgradeInventory eaep$getUpgradeInventory() {
        return eaep$upgradeInventory;
    }
}