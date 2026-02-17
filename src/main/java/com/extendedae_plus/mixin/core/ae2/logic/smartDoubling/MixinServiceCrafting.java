package com.extendedae_plus.mixin.core.ae2.logic.smartDoubling;

import appeng.api.crafting.IPatternDetails;
import appeng.me.service.CraftingService;
import com.extendedae_plus.util.extension.ExtensionScaledPattern;
import lombok.experimental.ExtensionMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@ExtensionMethod(ExtensionScaledPattern.class)
@Mixin(CraftingService.class)
public class MixinServiceCrafting {
    @ModifyVariable(method = "getProviders", at = @At("HEAD"), argsOnly = true)
    private IPatternDetails toOriginal(IPatternDetails instance) {
        return instance.original();
    }
}
