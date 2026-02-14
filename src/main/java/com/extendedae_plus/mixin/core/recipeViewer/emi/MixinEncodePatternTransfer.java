package com.extendedae_plus.mixin.core.recipeViewer.emi;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiEncodePatternHandler;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.client.impl.AliasGetter;
import com.extendedae_plus.integration.recipeViewer.emi.EmiRecipeAdaptable;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.util.extension.ExtensionMiscKt;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@MixinDependencies("emi")
@Mixin(EmiEncodePatternHandler.class)
public abstract class MixinEncodePatternTransfer {
    @Inject(method = "transferRecipe(Lappeng/menu/AEBaseMenu;Lnet/minecraft/world/item/crafting/RecipeHolder;Ldev/emi/emi/api/recipe/EmiRecipe;Z)Lappeng/integration/modules/emi/AbstractRecipeHandler$Result;",
            at = @At("HEAD"))
    private static void onTransfer(AEBaseMenu menu, RecipeHolder<?> holder,
                                   EmiRecipe emiRecipe, boolean doTransfer, CallbackInfoReturnable<?> cir) {
        if (!doTransfer) return;
        if (ExtensionMiscKt.isNonProcessing(EmiRecipeAdaptable.unbox(emiRecipe))) return;

        AliasGetter.tryCollectKeywords(emiRecipe);
    }

    @Redirect(method = "transferRecipe(Lappeng/menu/me/items/PatternEncodingTermMenu;Lnet/minecraft/world/item/crafting/RecipeHolder;Ldev/emi/emi/api/recipe/EmiRecipe;Z)Lappeng/integration/modules/emi/AbstractRecipeHandler$Result;",
            at = @At(value = "INVOKE", target = "Lappeng/integration/modules/itemlists/EncodingHelper;encodeCraftingRecipe(Lappeng/menu/me/items/PatternEncodingTermMenu;Lnet/minecraft/world/item/crafting/RecipeHolder;Ljava/util/List;Ljava/util/function/Predicate;)V"),
            require = 0)
    private static void encodeNonHolderRecipe(PatternEncodingTermMenu menu,
                                              @Nullable RecipeHolder<?> recipeVanilla,
                                              List<List<GenericStack>> ingredients,
                                              Predicate<ItemStack> predicateVisible,
                                              PatternEncodingTermMenu $,
                                              RecipeHolder<?> holder,
                                              EmiRecipe recipe) {
        if (recipe instanceof EmiRecipeAdaptable && recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING)
            recipeVanilla = null;
        EncodingHelper.encodeCraftingRecipe(menu, recipeVanilla, ingredients, predicateVisible);
    }
}
