package com.extendedae_plus.mixin.recipeViewer.emi;

import com.extendedae_plus.integration.recipeViewer.emi.GetEmiRecipeSearchKey;
import com.extendedae_plus.util.ExtendedAEPatternUploadUtil;
import com.gregtechceu.gtceu.integration.emi.recipe.Ae2PatternTerminalHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ae2PatternTerminalHandler.class)
public class MixinGtRecipeTransfer {
    @Inject(method = "craft", at = @At("HEAD"), remap = false)
    private void onTransfer(EmiRecipe recipe, EmiCraftContext<?> context, CallbackInfoReturnable<Boolean> cir) {
        ExtendedAEPatternUploadUtil.addLastProcessingNameList(GetEmiRecipeSearchKey.tryGetSearchKeys(recipe));
    }
}
