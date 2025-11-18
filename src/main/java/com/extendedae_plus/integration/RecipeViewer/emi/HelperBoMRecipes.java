package com.extendedae_plus.integration.RecipeViewer.emi;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.FoldState;
import dev.emi.emi.bom.MaterialNode;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelperBoMRecipes {
    public static HashMap<ResourceLocation, GenericStack> collectInputs(MaterialNode parentNode, long batches) {
        var recipe = parentNode.recipe;
        if (recipe == null) return new HashMap<>();
        if (parentNode.state != FoldState.EXPANDED) return new HashMap<>();
        if (parentNode.children == null) return new HashMap<>();

        var mappedResult = new HashMap<ResourceLocation, GenericStack>();

        parentNode.children.forEach(child -> {
            var stack = batchAmount(child.ingredient.getEmiStacks().getFirst(), batches);
            recipe.getInputs().forEach(input -> {
                if (!input.getEmiStacks().contains(stack)) return;

                var genericStack = EmiStackHelper.toGenericStack(stack);
                if (genericStack == null) return;

                mappedResult.put(input.getEmiStacks().getFirst().getId(), genericStack);
            });
        });

        return mappedResult;
    }

    public static List<List<GenericStack>> updateRecipe(EmiRecipe original,
                                                        HashMap<ResourceLocation, GenericStack> selectedInputs) {
        List<List<GenericStack>> modifiedInputs = new ArrayList<>();

        EmiStackHelper.ofInputs(original).forEach(stacks -> {
            if (!stacks.isEmpty()) {
                GenericStack originStack = stacks.getFirst();
                ResourceLocation stackId = originStack.what().getId();

                if (selectedInputs.containsKey(stackId))
                    modifiedInputs.add(List.of(
                            selectedInputs.getOrDefault(stackId, originStack)));
            } else modifiedInputs.add(List.of());
        });
        return modifiedInputs;
    }

    public static EmiStack batchAmount(EmiStack original, long batches) {
        return original.copy().setAmount(original.getAmount() * batches);
    }
}
