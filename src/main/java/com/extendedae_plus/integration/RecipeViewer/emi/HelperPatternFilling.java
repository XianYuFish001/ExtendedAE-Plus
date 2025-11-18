package com.extendedae_plus.integration.RecipeViewer.emi;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Predicate;

public class HelperPatternFilling {
    public static void encodeCraftingRecipe(PatternEncodingTermMenu menu,
                                            EmiRecipe recipe,
                                            List<List<GenericStack>> genericIngredients,
                                            Predicate<ItemStack> visiblePredicate) {
        if (VanillaEmiRecipeCategories.STONECUTTING.equals(recipe.getCategory())) {
            if (recipe.getBackingRecipe() == null) return;
            menu.setMode(EncodingMode.STONECUTTING);
            menu.setStonecuttingRecipeId(recipe.getBackingRecipe().id());
        } else if (VanillaEmiRecipeCategories.SMITHING.equals(recipe.getCategory()))
            menu.setMode(EncodingMode.SMITHING_TABLE);
        else menu.setMode(EncodingMode.CRAFTING);

        var encodedInputs = NonNullList.withSize(menu.getCraftingGridSlots().length, ItemStack.EMPTY);

        for (int slot = 0; slot < genericIngredients.size(); slot++) {
            var genericIngredient = genericIngredients.get(slot);
            if (genericIngredient.isEmpty()) continue;

            GenericStack selectedStack = null;
            for (var stack : genericIngredient) {
                if (stack.what() instanceof AEItemKey itemKey) {
                    ItemStack itemStack = itemKey.toStack();
                    if (visiblePredicate.test(itemStack)) {
                        selectedStack = stack;
                        break;
                    }
                }
            }

            if (selectedStack == null) selectedStack = genericIngredient.getFirst();

            if (selectedStack.what() instanceof AEItemKey itemKey)
                encodedInputs.set(slot, itemKey.toStack());
            else encodedInputs.set(slot, GenericStack.wrapInItemStack(selectedStack.what(), 1));
        }

        for (int i = 0; i < encodedInputs.size(); i++) {
            ItemStack encodedInput = encodedInputs.get(i);
            ServerboundPacket message = new InventoryActionPacket(
                    InventoryAction.SET_FILTER, menu.getCraftingGridSlots()[i].index, encodedInput);
            PacketDistributor.sendToServer(message);
        }

        for (var outputSlot : menu.getProcessingOutputSlots()) {
            ServerboundPacket message = new InventoryActionPacket(
                    InventoryAction.SET_FILTER, outputSlot.index, ItemStack.EMPTY);
            PacketDistributor.sendToServer(message);
        }
    }

    public static void encodeProcessingRecipe(PatternEncodingTermMenu menu, List<List<GenericStack>> genericIngredients,
                                              List<GenericStack> genericResults) {
        menu.setMode(EncodingMode.PROCESSING);

        for (int i = 0; i < menu.getProcessingInputSlots().length; i++) {
            ItemStack stackToSet = ItemStack.EMPTY;

            if (i < genericIngredients.size() && !genericIngredients.get(i).isEmpty()) {
                GenericStack selectedStack = genericIngredients.get(i).getFirst();
                stackToSet = GenericStack.wrapInItemStack(selectedStack);
            }

            ServerboundPacket message = new InventoryActionPacket(
                    InventoryAction.SET_FILTER, menu.getProcessingInputSlots()[i].index, stackToSet);
            PacketDistributor.sendToServer(message);
        }

        for (int i = 0; i < menu.getProcessingOutputSlots().length; i++) {
            ItemStack stackToSet = ItemStack.EMPTY;

            if (i < genericResults.size()) {
                GenericStack resultStack = genericResults.get(i);
                stackToSet = GenericStack.wrapInItemStack(resultStack);
            }

            ServerboundPacket message = new InventoryActionPacket(
                    InventoryAction.SET_FILTER, menu.getProcessingOutputSlots()[i].index, stackToSet);
            PacketDistributor.sendToServer(message);
        }
    }
}
