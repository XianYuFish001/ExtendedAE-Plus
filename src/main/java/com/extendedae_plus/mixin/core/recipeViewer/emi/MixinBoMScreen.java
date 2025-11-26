package com.extendedae_plus.mixin.core.recipeViewer.emi;

import appeng.integration.modules.emi.EmiStackHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.client.impl.AliasGetter;
import com.extendedae_plus.integration.recipeViewer.emi.HelperBoMRecipes;
import com.extendedae_plus.integration.recipeViewer.emi.HelperPatternFilling;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.core.recipeViewer.emi.accessor.AccessorBoMScreenHover;
import com.extendedae_plus.mixin.impl.bridge.BridgePlanToEncode;
import com.extendedae_plus.network.CPacketRequestUploading;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@MixinDependencies("emi")
@Mixin(BoMScreen.class)
public abstract class MixinBoMScreen {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!(button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && Screen.hasControlDown())) return;
        if (Minecraft.getInstance().player == null ||
                !(Minecraft.getInstance().player.containerMenu instanceof PatternEncodingTermMenu menu)) return;
        var self = (BoMScreen) (Object) this;

        var hover = self.getHoveredStack((int) mouseX, (int) mouseY);
        if (!(hover instanceof AccessorBoMScreenHover accessor)) return;
        var node = accessor.eaep$getHoverNode();
        if (node.recipe == null) return;

        menu.clear();

        if (VanillaEmiRecipeCategories.STONECUTTING.equals(node.recipe.getCategory())) return;

        boolean flagNonProcessing = VanillaEmiRecipeCategories.CRAFTING.equals(node.recipe.getCategory())
                || VanillaEmiRecipeCategories.SMITHING.equals(node.recipe.getCategory());

        if (flagNonProcessing) {
            HelperPatternFilling.encodeCraftingRecipe(menu,
                    node.recipe,
                    HelperBoMRecipes.updateRecipe(node.recipe,
                            HelperBoMRecipes.collectInputs(node, 1)),
                    stack -> true);
        } else {
            List<EmiStack> outputs = new ArrayList<>(node.recipe.getOutputs());
            EmiStack nodeStack = outputs.get(outputs.indexOf(node.ingredient.getEmiStacks().getFirst()));
            outputs.remove(nodeStack);
            outputs.addFirst(nodeStack);
            HelperPatternFilling.encodeProcessingRecipe(menu,
                    HelperBoMRecipes.updateRecipe(node.recipe,
                            HelperBoMRecipes.collectInputs(node, BoM.tree.batches)),
                    outputs.stream()
                            .map(stack -> HelperBoMRecipes.batchAmount(stack, BoM.tree.batches))
                            .map(EmiStackHelper::toGenericStack).toList());

            AliasGetter.tryCollectKeywords(node.recipe);
            PacketDistributor.sendToServer(CPacketRequestUploading.INSTANCE);
        }

        Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value());

        if (menu instanceof BridgePlanToEncode bridge)
            bridge.eaep$plan();

        cir.cancel();
    }
}
