package com.extendedae_plus.mixin.core.advancedae.menu;

import appeng.api.config.YesNo;
import appeng.menu.guisync.GuiSync;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.registry.settings.StateSmartBlocking;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartBlocking;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartDoubling;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvPatternProviderMenu.class)
public class MixinAdvProviderSettingsSyncing implements SyncerSmartBlocking, SyncerSmartDoubling {
    @Shadow
    @Final
    protected AdvPatternProviderLogic logic;

    @Unique
    @GuiSync(723)
    public StateSmartBlocking eaep$stateBlocking = StateSmartBlocking.DISABLED;
    @Unique
    @GuiSync(724)
    public YesNo eaep$stateDoubling = YesNo.NO;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void onChange(CallbackInfo ci) {
        if (((AdvPatternProviderMenu)(Object) this).isClientSide()) return;

        var configManager = this.logic.getConfigManager();
        this.eaep$stateBlocking = configManager.getSetting(ModSettings.smartBlocking);
        this.eaep$stateDoubling = configManager.getSetting(ModSettings.smartDoubling);
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
