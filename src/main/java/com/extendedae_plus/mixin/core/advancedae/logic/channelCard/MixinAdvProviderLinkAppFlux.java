package com.extendedae_plus.mixin.core.advancedae.logic.channelCard;

import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderUpgradesInv;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinDependencies("appflux")
@Mixin(value = AdvPatternProviderLogic.class, priority = 1100)
public class MixinAdvProviderLinkAppFlux implements HelperProviderUpgradesInv {
    @Unique
    private static final Logger eaep$LOGGER = LogUtils.getLogger();
    @Unique
    private Runnable eaep$onUpgradesChanged;

    @Shadow
    private IUpgradeInventory af_upgrades;

    @Shadow
    private void af_onUpgradesChanged() {}

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, AdvPatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        try {
            this.af_upgrades = UpgradeInventories.forMachine(
                    host.getTerminalIcon().getItem(), Math.min(this.af_upgrades.size() + 1, 8), this::af_onUpgradesChanged);
        } catch (Throwable ignore) {
        }
    }

    @Inject(method = "af_onUpgradesChanged", at = @At("HEAD"))
    private void eaep$onUpgradesChanged(CallbackInfo ci) {
        try {
            this.eaep$onUpgradesChanged.run();
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] AdvPatternProvider 初始化频道链接失败", throwable);
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void onReadingComponents(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        try {
            this.eaep$onUpgradesChanged.run();
        } catch (Throwable throwable) {
            eaep$LOGGER.warn("[EAEP] AdvPatternProvider 初始化频道链接失败", throwable);
        }
    }

    @Override
    public void eaep$bindAction(Runnable action) {
        this.eaep$onUpgradesChanged = action;
    }

    @Override
    public IUpgradeInventory eaep$getUpgradeInventory() {
        return af_upgrades;
    }
}
