package com.extendedae_plus.integration.recipeViewer.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.jemi.JemiRecipe;
import lombok.AccessLevel;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiRecipeAdaptable implements EmiRecipe {
    private final EmiRecipe recipe;
    @Setter(AccessLevel.PACKAGE)
    private List<EmiIngredient> inputs;

    public EmiRecipeAdaptable(EmiRecipe recipe) {
        if (recipe instanceof EmiRecipeAdaptable)
            throw new IllegalArgumentException("Cannot wrap a wrapped EmiRecipe");
        this.recipe = recipe;
    }

    public static EmiRecipeAdaptable of(EmiRecipe recipe, MaterialNode nodeParent, long batches) {
        var adaptable = new EmiRecipeAdaptable(recipe);
        adaptable.adapt(nodeParent, batches);
        return adaptable;
    }

    public static EmiRecipe unbox(EmiRecipe boxed) {
        if (boxed instanceof EmiRecipeAdaptable adaptable)
            return adaptable.recipe;
        else return boxed;
    }

    public static @Nullable JemiRecipe<?> unboxJemi(Object boxed) {
        if (boxed instanceof JemiRecipe<?> recipeJemi)
            return recipeJemi;
        else if (boxed instanceof EmiRecipeAdaptable adaptable)
            return unboxJemi(adaptable.recipe);
        else
            return null;
    }

    public void adapt(MaterialNode nodeParent, long batches) {
        var mapped = HelperBoMRecipes.mapChildren(nodeParent, batches);
        HelperBoMRecipes.applyMappings(this, this.recipe, mapped);
        this.getOutputs().forEach(output -> output.setAmount(output.getAmount() * batches));
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs == null
                ? this.recipe.getInputs()
                : this.inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.recipe.getOutputs();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return this.recipe.getCategory();
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.recipe.getId();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return this.recipe.getCatalysts();
    }

    @Override
    public int getDisplayWidth() {
        return this.recipe.getDisplayWidth();
    }

    @Override
    public int getDisplayHeight() {
        return this.recipe.getDisplayHeight();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        this.recipe.addWidgets(widgets);
    }

    @Override
    public boolean supportsRecipeTree() {
        return this.recipe.supportsRecipeTree();
    }

    @Override
    public boolean hideCraftable() {
        return this.recipe.hideCraftable();
    }

    @Override
    public @Nullable RecipeHolder<?> getBackingRecipe() {
        return this.recipe.getBackingRecipe();
    }
}
