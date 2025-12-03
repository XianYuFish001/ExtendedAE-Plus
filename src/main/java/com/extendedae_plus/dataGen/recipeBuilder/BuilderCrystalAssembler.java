package com.extendedae_plus.dataGen.recipeBuilder;

import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class BuilderCrystalAssembler extends CrystalAssemblerRecipeBuilder {
    public BuilderCrystalAssembler(ItemStack output) {
        super(output);
    }

    public static BuilderCrystalAssembler of(ItemStack item, Consumer<BuilderCrystalAssembler> consumer) {
        var builder = new BuilderCrystalAssembler(item);
        consumer.accept(builder);
        return builder;
    }

    public static BuilderCrystalAssembler of(ItemLike item, int count, Consumer<BuilderCrystalAssembler> consumer) {
        return of(new ItemStack(item, count), consumer);
    }

    public static BuilderCrystalAssembler of(ItemLike item, Consumer<BuilderCrystalAssembler> consumer) {
        return of(new ItemStack(item), consumer);
    }

    public BuilderCrystalAssembler input(Ingredient ingredient, int count) {
        this.inputs.add(IngredientStack.of(ingredient, count));
        return this;
    }
}
