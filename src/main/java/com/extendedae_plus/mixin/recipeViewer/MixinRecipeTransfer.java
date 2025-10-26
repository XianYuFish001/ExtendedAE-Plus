package com.extendedae_plus.mixin.recipeViewer;

import appeng.integration.modules.jei.transfer.EncodePatternTransferHandler;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.util.ExtendedAEPatternUploadUtil;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 捕获通过 JEI 点击 + 填充到样板编码终端的处理配方，并记录其工艺名称（如“烧炼”）。
 */
@Mixin(value = EncodePatternTransferHandler.class, remap = false)
public abstract class MixinRecipeTransfer {

    @Inject(method = "transferRecipe", at = @At("HEAD"), require = 0)
    private void eap$captureProcessingName(PatternEncodingTermMenu menu,
                                                       Object recipeBase,
                                                       IRecipeSlotsView slotsView,
                                                       Player player,
                                                       boolean maxTransfer,
                                                       boolean doTransfer,
                                                       CallbackInfoReturnable<IRecipeTransferError> cir) {
        if (!doTransfer) return;
        if (recipeBase == null) return;

        List<String> keys = new ArrayList<>();
        if (recipeBase instanceof Recipe<?> recipe && !EncodingHelper.isSupportedCraftingRecipe(recipe)) {
            eaep$addIfPresent(keys, ExtendedAEPatternUploadUtil.mapRecipeTypeToSearchKey(recipe));
        }

        // gtceu的emi适配有点香(

        try {
            Method tryGetEmiSearchKeys = Class.forName(
                    "com.extendedae_plus.integration.recipeViewer.emi.GetEmiRecipeSearchKey")
                    .getMethod("tryGetSearchKeys", Object.class);
            List<String> emiKeys = (List<String>) tryGetEmiSearchKeys.invoke(null, recipeBase);
            if (!emiKeys.isEmpty())
                keys.addAll(emiKeys);
        } catch (Throwable ignore) {
        }

        eaep$addIfPresent(keys, ExtendedAEPatternUploadUtil.deriveSearchKeyFromUnknownRecipe(recipeBase));

        // 特殊情况特殊处理.jpg
        keys.remove("jemi");
        keys.remove("jei:");

        ExtendedAEPatternUploadUtil.addLastProcessingNameList(keys);
    }

    @Unique
    private <T> void eaep$addIfPresent(List<T> list, T key) {
        if (key != null) list.add(key);
    }
}
