package com.extendedae_plus.mixin.core.recipeViewer.emi;

import appeng.client.gui.me.items.PatternEncodingTermScreen;
import com.extendedae_plus.integration.recipeViewer.emi.EmiRecipeAdaptable;
import com.extendedae_plus.mixin.bridge.BridgePlanToEncode;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiRecipeFiller.class)
public class MixinRecipeFiller {
    @Inject(method = "performFill", at = @At("RETURN"))
    private static void performFill(EmiRecipe recipe,
                                    AbstractContainerScreen<?> screen,
                                    EmiCraftContext.Type type,
                                    EmiCraftContext.Destination destination,
                                    int amount,
                                    CallbackInfoReturnable<Boolean> cir) {
        if (!(screen instanceof PatternEncodingTermScreen<?> screenEncode)) return;
        var menu = screenEncode.getMenu();
        if (!(menu instanceof BridgePlanToEncode helper)) return;
        if (!helper.eaep$planned()) return;
        menu.encode();
    }

    @Redirect(method = "performFill",
            at = @At(value = "INVOKE",
                    target = "Ldev/emi/emi/api/recipe/handler/EmiRecipeHandler;craft(Ldev/emi/emi/api/recipe/EmiRecipe;Ldev/emi/emi/api/recipe/handler/EmiCraftContext;)Z"))
    private static <T extends AbstractContainerMenu> boolean
    cancelBack(EmiRecipeHandler<T> instance, EmiRecipe recipe, EmiCraftContext<T> context) {
        var crafted = instance.craft(recipe, context);
        return !(recipe instanceof EmiRecipeAdaptable) && crafted;
    }
}
