package com.extendedae_plus.mixin.core.ae2.logic;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.util.ConfigManager;
import com.extendedae_plus.common.init.EAEPSettings;
import com.extendedae_plus.common.registry.settings.StateSmartBlocking;
import com.extendedae_plus.mixin.impl.ProviderSettingsImplementations;
import com.fish.fishlib.mixin.MixinDependencies;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@MixinDependencies(conflict = "expandedae")
@Mixin(value = PatternProviderLogic.class, priority = 1100)
public abstract class MixinProviderSettings {
    @Shadow
    @Final
    private List<IPatternDetails> patterns;
    @Shadow
    @Final
    private PatternProviderLogicHost host;

    @Shadow
    public abstract IConfigManager getConfigManager();
    @Shadow
    public abstract void updatePatterns();
    @Shadow
    public abstract boolean isBlocking();
    @Shadow
    public abstract boolean isClientSide();

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        var configBuilder = (ConfigManager) this.getConfigManager();
        configBuilder.registerSetting(EAEPSettings.smartBlocking, StateSmartBlocking.DISABLED_BY_SUPER);
        configBuilder.registerSetting(EAEPSettings.smartDoubling, YesNo.NO);

        if (this.host.getBlockEntity() == null || this.isClientSide()) return;

        if (YesNo.YES.equals(this.getConfigManager().getSetting(Settings.BLOCKING_MODE)))
            configBuilder.putSetting(EAEPSettings.smartBlocking, StateSmartBlocking.DISABLED);
    }

    @Inject(method = "configChanged", at = @At("HEAD"))
    private void onSettingsChanged(IConfigManager manager, Setting<?> setting, CallbackInfo ci) {
        if (this.host.getBlockEntity() == null || this.isClientSide()) return;
        ProviderSettingsImplementations.onSettingsChanged(manager, setting, this::updatePatterns);
    }

    @Redirect(method = "pushPattern", at = @At(value = "INVOKE",
            target = "Lappeng/helpers/patternprovider/PatternProviderTarget;containsPatternInput(Ljava/util/Set;)Z"))
    private boolean onPatternPushing(PatternProviderTarget target, Set<AEKey> inputs, IPatternDetails patternDetails) {
        return ProviderSettingsImplementations.checkCanBlock(
                this.isBlocking(), this.getConfigManager(), target, inputs, patternDetails);
    }

    @Inject(method = "updatePatterns", at = @At("TAIL"))
    private void onPatternsUpdating(CallbackInfo ci) {
        ProviderSettingsImplementations.updateDoublingState(this.getConfigManager(), this.patterns);
    }
}
