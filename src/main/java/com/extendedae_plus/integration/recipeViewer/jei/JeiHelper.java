package com.extendedae_plus.integration.recipeViewer.jei;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import com.extendedae_plus.integration.recipeViewer.IRecipeViewerHelper;
import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.Collections;
import java.util.List;

public class JeiHelper implements IRecipeViewerHelper {
    @Override
    public List<GenericStack> getHoveredStacks(double mouseX, double mouseY) {
        return getHoveredStacks();
    }

    @Override
    public List<GenericStack> getHoveredStacks() {
        ITypedIngredient<?> hovered = JeiRuntimeProxy.getIngredientUnderMouse().orElse(null);
        if (hovered != null)
            return Collections.singletonList(GenericEntryStackHelper.ingredientToStack(hovered));
        else return null;
    }

    @Override
    public List<GenericStack> getFavorites() {
        return JeiRuntimeProxy.getBookmarkList().stream()
                .map(GenericEntryStackHelper::ingredientToStack).toList();
    }

    @Override
    public boolean isCheatMode() {
        return JeiRuntimeProxy.isJeiCheatModeEnabled();
    }

    @Override
    public void addFavorite(GenericStack stack) {
        JeiRuntimeProxy.addBookmark(stack);
    }

    @Override
    public void setSearch(String text) {
        JeiRuntimeProxy.setIngredientFilterText(text);
    }
}
