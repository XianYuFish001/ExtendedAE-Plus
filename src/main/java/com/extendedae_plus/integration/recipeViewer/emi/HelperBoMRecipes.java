package com.extendedae_plus.integration.recipeViewer.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.FoldState;
import dev.emi.emi.bom.MaterialNode;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;

public class HelperBoMRecipes {
    public static HashMap<ResourceLocation, EmiStack> mapChildren(MaterialNode nodeParent, long batches) {
        var recipe = nodeParent.recipe;
        if (recipe == null) return new HashMap<>();
        if (nodeParent.state != FoldState.EXPANDED) return new HashMap<>();
        if (nodeParent.children == null) return new HashMap<>();

        var resultMapped = new HashMap<ResourceLocation, EmiStack>();

        nodeParent.children.forEach(child -> {
            var stack = batchAmount(child, batches);
            recipe.getInputs().forEach(input -> {
                if (!input.getEmiStacks().contains(stack)) return;
                resultMapped.put(input.getEmiStacks().getFirst().getId(), stack);
            });
        });

        return resultMapped;
    }

    public static void applyMappings(EmiRecipeAdaptable adaptable,
                                     EmiRecipe original,
                                     HashMap<ResourceLocation, EmiStack> mapper) {
        var mapped = new ArrayList<EmiIngredient>();
        original.getInputs().forEach(input -> {
            var key = input.getEmiStacks().getFirst().getId();
            var stack = mapper.get(key);
            mapped.add(stack == null ? input : stack);
        });
        adaptable.setInputs(mapped);
    }

    public static EmiStack batchAmount(MaterialNode node, long batches) {
        EmiStack original;
        if (node.recipe instanceof EmiResolutionRecipe recipeResolution)
            original = recipeResolution.stack;
        else original = node.ingredient.getEmiStacks().getFirst();
        return original.copy().setAmount(node.amount * batches);
    }
}
