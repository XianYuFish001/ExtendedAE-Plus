package com.extendedae_plus.util.extension;

import com.extendedae_plus.mixin.core.extendedae.accessor.AccessorBuilderCrystalAssembler;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ExtensionBuilderRecipe {
    public static CrystalAssemblerRecipeBuilder input(CrystalAssemblerRecipeBuilder instance,
                                                      Ingredient ingredient,
                                                      int count) {
        ((AccessorBuilderCrystalAssembler) instance).getInputs().add(IngredientStack.of(ingredient, count));
        return instance;
    }
}
