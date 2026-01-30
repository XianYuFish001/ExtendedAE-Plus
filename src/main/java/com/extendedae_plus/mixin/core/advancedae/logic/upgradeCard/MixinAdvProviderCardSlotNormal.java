package com.extendedae_plus.mixin.core.advancedae.logic.upgradeCard;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.bridge.HelperProviderUpgradesInv;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@MixinDependencies(conflict = "appflux")
@Mixin(AdvPatternProviderLogic.class)
public class MixinAdvProviderCardSlotNormal implements HelperProviderUpgradesInv {
    @Unique
    private static final Logger eaep$LOGGER = LogUtils.getLogger();
    @Unique
    private IUpgradeInventory eaep$upgradeInventory = UpgradeInventories.empty();
    @Unique
    private final Set<Runnable> eaep$onUpgradesChanged = new HashSet<>();

    @Shadow
    @Final
    private AdvPatternProviderLogicHost host;

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.eaep$upgradeInventory = UpgradeInventories.forMachine(
                host.getTerminalIcon().getItem(), 2, this::eaep$onUpgradesChanged);
    }

    @Unique
    private void eaep$onUpgradesChanged() {
        try {
            this.eaep$onUpgradesChanged.forEach(Runnable::run);
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] AdvPatternProvider 初始化频道链接失败", throwable);
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
            this.eaep$onUpgradesChanged.forEach(Runnable::run);
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] AdvPatternProvider 初始化频道链接失败", throwable);
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
    public void eaep$addAction(Runnable action) {
        this.eaep$onUpgradesChanged.add(action);
    }

    @Override
    public IUpgradeInventory eaep$getUpgradeInventory() {
        return eaep$upgradeInventory;
    }
}
