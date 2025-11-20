package com.extendedae_plus.integration.recipeViewer;

import appeng.api.stacks.GenericStack;
import com.extendedae_plus.integration.recipeViewer.emi.EmiHelper;
import com.extendedae_plus.integration.recipeViewer.jei.JeiHelper;
import com.mojang.datafixers.util.Pair;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Optional;

public class HelperRecipeViewer {
    private static IHelperRecipeViewer activeViewer;

    public static void init() {
        if (ModList.get().isLoaded("emi")) activeViewer = new EmiHelper();
        else if (ModList.get().isLoaded("jei")) activeViewer = new JeiHelper();
        else activeViewer = new EmptyHelper();
    }

    public static Optional<IHelperRecipeViewer> getViewer() {
        if (activeViewer == null) init();
        return Optional.ofNullable(activeViewer);
    }

    public static List<GenericStack> getHoveredStacks() {
        return getViewer().map(IHelperRecipeViewer::getHoveredStacks).orElse(List.of());
    }

    public static List<GenericStack> getFavorites() {
        return getViewer().map(IHelperRecipeViewer::getFavorites).orElse(List.of());
    }

    public static Pair<Integer, Boolean> getPulled(int mouseKey) {
        return getViewer().map(viewer -> viewer.getPulled(mouseKey)).orElse(new Pair<>(0, false));
    }

    public static boolean isCheatMode() {
        return getViewer().map(IHelperRecipeViewer::isCheatMode).orElse(false);
    }

    public static void addFavorite(GenericStack stack) {
        getViewer().ifPresent(viewer -> viewer.addFavorite(stack));
    }

    public static void setSearchText(String text) {
        getViewer().ifPresent(viewer -> viewer.setSearch(text));
    }
}
