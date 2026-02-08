package com.extendedae_plus.mixin.core.recipeViewer.emi;

import com.extendedae_plus.integration.recipeViewer.emi.EmiRecipeAdaptable;
import com.extendedae_plus.util.extension.ExtensionEmi;
import dev.emi.emi.api.recipe.EmiRecipe;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@ExtensionMethod(ExtensionEmi.class)
@Mixin(targets = "appeng.integration.modules.emi.AbstractRecipeHandler")
public class MixinHandlerRecipe {
    @Redirect(method = "transferRecipe(Ldev/emi/emi/api/recipe/EmiRecipe;Ldev/emi/emi/api/recipe/handler/EmiCraftContext;Z)Lappeng/integration/modules/emi/AbstractRecipeHandler$Result;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void cancelBack(Minecraft instance, Screen old, EmiRecipe recipe) {
        if (recipe instanceof EmiRecipeAdaptable) return;
        instance.setScreen(old);
    }
}
