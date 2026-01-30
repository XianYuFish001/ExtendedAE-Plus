package com.extendedae_plus.integration.recipeViewer;

import appeng.api.stacks.GenericStack;
import com.extendedae_plus.integration.ContextModLoaded;
import com.extendedae_plus.integration.recipeViewer.emi.ViewerEmi;
import com.extendedae_plus.integration.recipeViewer.jei.ViewerJei;
import com.mojang.datafixers.util.Pair;

import java.util.List;

public class HelperRecipeViewer {
    private static IRecipeViewer viewer;

    public static void init() {
        if (ContextModLoaded.emi.isLoaded()) viewer = new ViewerEmi();
        else if (ContextModLoaded.jei.isLoaded()) viewer = new ViewerJei();
        else viewer = new ViewerEmpty();
    }

    public static IRecipeViewer getViewer() {
        if (viewer == null) init();
        return viewer;
    }

    public static List<GenericStack> getHoveredStacks() {
        return getViewer().getHoveredStacks();
    }

    public static List<GenericStack> getFavorites() {
        return getViewer().getFavorites();
    }

    public static Pair<Integer, Boolean> getPulled(int mouseKey) {
        return getViewer().getPulled(mouseKey);
    }

    public static boolean isCheatMode() {
        return getViewer().isCheatMode();
    }

    public static void addFavorite(GenericStack stack) {
        getViewer().addFavorite(stack);
    }

    public static void setSearchText(String text) {
        getViewer().setSearch(text);
    }
}
