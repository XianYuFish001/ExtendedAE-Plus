package com.extendedae_plus.integration.RecipeViewer.emi;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import com.extendedae_plus.integration.RecipeViewer.IRecipeViewerHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;

import java.util.List;

public class EMIHelper implements IRecipeViewerHelper {
    @Override
    public List<GenericStack> getHoveredStacks(double mouseX, double mouseY) {
        return EmiApi.getHoveredStack((int) mouseX, (int) mouseY, false).getStack().getEmiStacks()
                .stream().map(EmiStackHelper::toGenericStack).toList();
    }

    @Override
    public List<GenericStack> getHoveredStacks() {
        return EmiApi.getHoveredStack(false).getStack().getEmiStacks()
                .stream().map(EmiStackHelper::toGenericStack).toList();
    }

    @Override
    public List<GenericStack> getFavorites() {
        return EmiFavorites.favorites.stream()
                .map(EmiFavorite::getEmiStacks)
                .map(List::getFirst)
                .map(EmiStackHelper::toGenericStack)
                .toList();
    }

    @Override
    public void addFavorite(GenericStack stack) {
        EmiFavorites.addFavorite(EmiStackHelper.toEmiStack(stack));
    }

    @Override
    public void setSearch(String text) {
        EmiApi.setSearchText(text);
    }
}
