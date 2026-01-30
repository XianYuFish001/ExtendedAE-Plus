package com.extendedae_plus.mixin.core.ae2.logic.upgradeCard;

import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.bridge.HelperProviderUpgradesInv;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@MixinDependencies("appflux")
@Mixin(value = PatternProviderLogic.class, priority = 1100)
public class MixinProviderCardSlotAppFlux implements HelperProviderUpgradesInv {
    @Unique
    private static final Logger eaep$LOGGER = LogUtils.getLogger();
    @Unique
    private final Set<Runnable> eaep$onUpgradesChanged = new HashSet<>();

    @Dynamic("appflux")
    @Shadow
    private IUpgradeInventory af_upgrades;

    @Dynamic("appflux")
    @Shadow
    private void af_onUpgradesChanged() {}

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        try {
            this.af_upgrades = UpgradeInventories.forMachine(
                    host.getTerminalIcon().getItem(), Math.min(this.af_upgrades.size() + 2, 8), this::af_onUpgradesChanged);
        } catch (Throwable ignore) {
        }
    }

    @Dynamic("appflux")
    @Inject(method = "af_onUpgradesChanged", at = @At("HEAD"))
    private void eaep$onUpgradesChanged(CallbackInfo ci) {
        try {
            this.eaep$onUpgradesChanged.forEach(Runnable::run);
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] PatternProvider 初始化频道链接失败", throwable);
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void onReadingComponents(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        try {
            this.eaep$onUpgradesChanged.forEach(Runnable::run);
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] PatternProvider 初始化频道链接失败", throwable);
        }
    }

    @Override
    public void eaep$addAction(Runnable action) {
        this.eaep$onUpgradesChanged.add(action);
    }

    @Override
    public IUpgradeInventory eaep$getUpgradeInventory() {
        return af_upgrades;
    }
}
