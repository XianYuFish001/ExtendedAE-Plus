package com.extendedae_plus.integration.recipeViewer.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.jemi.JemiRecipe;

import java.util.ArrayList;
import java.util.List;

public class GetEmiRecipeSearchKey {
    public static List<String> tryGetSearchKeys(Object recipeObject) {
        List<String> keys = new ArrayList<>();
        if (recipeObject instanceof JemiRecipe<?> jemiRecipe) {
            keys.add(jemiRecipe.category.getTitle().getString());
            keys.add(jemiRecipe.originalId.toString().split("/")[0]);
        } else if (recipeObject instanceof EmiRecipe emiRecipe) {
            keys.add(emiRecipe.getCategory().getName().getString());
            keys.add(emiRecipe.getId().toString().split("/")[0]);
        }

        return keys;
    }
}
