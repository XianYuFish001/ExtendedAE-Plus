package com.extendedae_plus.mixin.core.extendedae.accessor;

import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CrystalAssemblerRecipeBuilder.class)
public interface AccessorBuilderCrystalAssembler {
    @Accessor("inputs")
    List<IngredientStack.Item> getInputs();
}
