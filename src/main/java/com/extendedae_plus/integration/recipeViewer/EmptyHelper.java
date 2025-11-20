package com.extendedae_plus.integration.recipeViewer;

import appeng.api.stacks.GenericStack;
import com.mojang.datafixers.util.Pair;

import java.util.List;

public class EmptyHelper implements IHelperRecipeViewer {
    @Override
    public List<GenericStack> getHoveredStacks(double mouseX, double mouseY) {
        return List.of();
    }

    @Override
    public List<GenericStack> getHoveredStacks() {
        return List.of();
    }

    @Override
    public List<GenericStack> getFavorites() {
        return List.of();
    }

    @Override
    public Pair<Integer, Boolean> getPulled(int mouseKey) {
        return new Pair<>(0, false);
    }

    @Override
    public void addFavorite(GenericStack stack) {
    }

    @Override
    public void setSearch(String text) {

    }
}
