package com.extendedae_plus.mixin.core.ae2.menu;

import appeng.api.config.YesNo;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.settings.StateSmartBlocking;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartBlocking;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartDoubling;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternProviderMenu.class)
public class MixinProviderSettingsSyncing implements SyncerSmartBlocking, SyncerSmartDoubling {
    @Shadow
    @Final
    protected PatternProviderLogic logic;

    @Unique
    @GuiSync(721)
    public StateSmartBlocking eaep$stateBlocking = StateSmartBlocking.DISABLED;
    @Unique
    @GuiSync(722)
    public YesNo eaep$stateDoubling = YesNo.NO;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void onChange(CallbackInfo ci) {
        if (((PatternProviderMenu)(Object) this).isClientSide()) return;

        var configManager = this.logic.getConfigManager();
        this.eaep$stateBlocking = configManager.getSetting(ModSettings.SMART_BLOCKING);
        this.eaep$stateDoubling = configManager.getSetting(ModSettings.SMART_DOUBLING);
    }

    @Override
    public StateSmartBlocking eaep$getBlockingState() {
        return this.eaep$stateBlocking;
    }

    @Override
    public YesNo eaep$getDoublingState() {
        return this.eaep$stateDoubling;
    }
}