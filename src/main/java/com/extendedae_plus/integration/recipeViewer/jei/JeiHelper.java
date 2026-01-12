package com.extendedae_plus.integration.recipeViewer.jei;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.extendedae_plus.integration.ContextModLoaded;
import com.extendedae_plus.integration.recipeViewer.IHelperRecipeViewer;
import com.mojang.datafixers.util.Pair;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import tamaized.ae2jeiintegration.integration.modules.jei.GenericEntryStackHelper;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class JeiHelper implements IHelperRecipeViewer {
    @Override
    public List<GenericStack> getHoveredStacks(double mouseX, double mouseY) {
        return getHoveredStacks();
    }

    @Override
    public List<GenericStack> getHoveredStacks() {
        ITypedIngredient<?> hovered = ProxyJeiRuntime.getIngredientUnderMouse().orElse(null);
        if (hovered != null)
            return Collections.singletonList(GenericEntryStackHelper.ingredientToStack(hovered));
        else return null;
    }

    @Override
    public List<GenericStack> getFavorites() {
        return ProxyJeiRuntime.getBookmarkList().stream()
                .map(GenericEntryStackHelper::ingredientToStack).toList();
    }

    @Override
    public Pair<Integer, Boolean> getPulled(int mouseKey) {
        int amount = 0;
        boolean toInv = true;

        if (Screen.hasControlDown()) amount = 1;
        if (amount > 0 && Screen.hasShiftDown()) amount = 64;

        if (mouseKey == GLFW.GLFW_MOUSE_BUTTON_RIGHT) toInv = false;

        return new Pair<>(amount, toInv);
    }

    @Override
    public boolean isCheatMode() {
        return ProxyJeiRuntime.isJeiCheatModeEnabled();
    }

    @Override
    public void addFavorite(GenericStack stack) {
        AEKey key = stack.what();
        if (key instanceof AEItemKey itemKey)
            ProxyJeiRuntime.addBookmark(itemKey.toStack());
        else if (key instanceof AEFluidKey fluidKey)
            ProxyJeiRuntime.addBookmark(fluidKey.toStack(1000));
        else if (ContextModLoaded.mekanism.isLoaded() && ContextModLoaded.appliedMekanistics.isLoaded()) {
            try {
                Class<?> keyClass = key.getClass();
                if (keyClass.getName().contains("MekanismKey")) {
                    Method getChemicalStackMethod = keyClass.getMethod("getStack");
                    Object chemicalStack = getChemicalStackMethod.invoke(key);
                    ProxyJeiRuntime.addBookmark(chemicalStack);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void setSearch(String text) {
        ProxyJeiRuntime.setIngredientFilterText(text);
    }
}
