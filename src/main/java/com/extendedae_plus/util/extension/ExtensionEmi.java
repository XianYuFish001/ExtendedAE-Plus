package com.extendedae_plus.util.extension;

import com.extendedae_plus.integration.recipeViewer.emi.EmiRecipeAdaptable;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;

public class ExtensionEmi {
    public static boolean isNonProcessing(EmiRecipe instance) {
        return VanillaEmiRecipeCategories.CRAFTING == instance.getCategory()
                || VanillaEmiRecipeCategories.SMITHING == instance.getCategory()
                || VanillaEmiRecipeCategories.STONECUTTING == instance.getCategory();
    }

    public static EmiRecipe unbox(EmiRecipe instance) {
        return EmiRecipeAdaptable.unbox(instance);
    }
}
