package com.extendedae_plus.mixin.core.ae2;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import com.extendedae_plus.common.init.ModSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Settings.class)
public class MixinSettingsInitialization {
    @Inject(method = "getOrThrow", at = @At("HEAD"), cancellable = true)
    private static void findSettingsOnEAEPRegistries(String name, CallbackInfoReturnable<Setting<?>> cir) {
        var eaep_settings = ModSettings.settings.get(name);
        if (eaep_settings == null) return;
        cir.setReturnValue(eaep_settings);
    }
}
