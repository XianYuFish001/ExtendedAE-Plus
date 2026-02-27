package com.extendedae_plus.mixin.core.recipeViewer.jei;

import appeng.integration.modules.itemlists.EncodingHelper;
import com.extendedae_plus.client.impl.AliasGetter;
import com.fish.fishlib.mixin.MixinDependencies;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tamaized.ae2jeiintegration.integration.modules.jei.transfer.EncodePatternTransferHandler;

@MixinDependencies(value = {"jei", "ae2jeiintegration"}, conflict = "emi")
@Mixin(EncodePatternTransferHandler.class)
public class MixinEncodePatternTransfer {
    @Inject(method = "transferRecipe(Lnet/minecraft/world/inventory/AbstractContainerMenu;Ljava/lang/Object;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/world/entity/player/Player;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;", at = @At("HEAD"))
    private void onTransfer(AbstractContainerMenu menu,
                            Object recipeBase,
                            IRecipeSlotsView slotsView,
                            Player player,
                            boolean maxTransfer,
                            boolean doTransfer,
                            CallbackInfoReturnable<IRecipeTransferError> cir) {
        if (!doTransfer) return;
        if (!(recipeBase instanceof RecipeHolder<?> holder)
                || EncodingHelper.isSupportedCraftingRecipe(holder.value()))
            return;

        AliasGetter.tryCollectKeywords(holder);
    }
}
