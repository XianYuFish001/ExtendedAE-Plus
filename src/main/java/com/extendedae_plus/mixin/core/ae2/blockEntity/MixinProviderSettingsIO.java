package com.extendedae_plus.mixin.core.ae2.blockEntity;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.util.SettingsFrom;
import com.extendedae_plus.common.impl.pattern.smartDoubling.SmartDoublingHolder;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.mixin.impl.DataProviderSettings;
import com.extendedae_plus.mixin.impl.bridge.AdvancedBlockingHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternProviderBlockEntity.class)
public class MixinProviderSettingsIO {
    @Shadow
    @Final
    protected PatternProviderLogic logic;

    @Inject(method = "exportSettings", at = @At("HEAD"))
    private void exportAdditionalSettings(SettingsFrom mode, DataComponentMap.Builder builder, Player player, CallbackInfo ci) {
        if (!mode.equals(SettingsFrom.MEMORY_CARD)) return;

        var smartDoubling = false;
        var smartBlocking = false;

        if (this.logic instanceof SmartDoublingHolder holder)
            smartDoubling = holder.eap$getSmartDoubling();
        if (this.logic instanceof AdvancedBlockingHolder holder)
            smartBlocking = holder.eap$getAdvancedBlocking();

        builder.set(ModDataComponents.DATA_PROVIDER_SETTINGS, new DataProviderSettings(smartDoubling, smartBlocking));
    }

    @Inject(method = "importSettings", at = @At("HEAD"))
    private void importAdditionalSettings(SettingsFrom mode, DataComponentMap input, Player player, CallbackInfo ci) {
        if (!mode.equals(SettingsFrom.MEMORY_CARD)) return;
        if (!input.has(ModDataComponents.DATA_PROVIDER_SETTINGS.get())) return;

        var settings = input.get(ModDataComponents.DATA_PROVIDER_SETTINGS.get());

        if (this.logic instanceof SmartDoublingHolder holder)
            holder.eap$setSmartDoubling(settings.smartDoubling());
        if (this.logic instanceof AdvancedBlockingHolder holder)
            holder.eap$setAdvancedBlocking(settings.smartBlocking());
    }
}
