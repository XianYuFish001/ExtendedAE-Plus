package com.extendedae_plus.integration.recipeViewer;

import appeng.api.stacks.GenericStack;
import com.mojang.datafixers.util.Pair;

import java.util.List;

public interface IHelperRecipeViewer {
    List<GenericStack> getHoveredStacks(double mouseX, double mouseY);

    List<GenericStack> getHoveredStacks();

    List<GenericStack> getFavorites();

    Pair<Integer, Boolean> getPulled(int mouseKey);

    default boolean isCheatMode() {
        return true;
    }

    void addFavorite(GenericStack stack);

    void setSearch(String text);
}
