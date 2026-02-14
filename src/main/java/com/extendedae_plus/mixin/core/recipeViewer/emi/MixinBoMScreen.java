package com.extendedae_plus.mixin.core.recipeViewer.emi;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.integration.recipeViewer.emi.EmiRecipeAdaptable;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.bridge.BridgePlanToEncode;
import com.extendedae_plus.mixin.core.recipeViewer.emi.accessor.AccessorBoMScreenHover;
import com.extendedae_plus.mixin.impl.HelperClientOnly;
import com.extendedae_plus.util.UtilObject;
import com.extendedae_plus.util.extension.ExtensionEmi;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.BoMScreen;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MixinDependencies("emi")
@Mixin(BoMScreen.class)
@ExtensionMethod(ExtensionEmi.class)
public abstract class MixinBoMScreen {
    @Shadow
    public AbstractContainerScreen<?> old;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return;
        if (!HelperClientOnly.instance.hasControlDown()) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.containerMenu instanceof PatternEncodingTermMenu menu)) return;

        var hovered = UtilObject.<BoMScreen>cast(this).getHoveredStack((int) mouseX, (int) mouseY);
        if (!(hovered instanceof AccessorBoMScreenHover accessor)) return;
        var node = accessor.getNode();
        var recipe = node.recipe;
        if (recipe == null) return;
        if (recipe.getCategory() == VanillaEmiRecipeCategories.STONECUTTING) return;

        menu.clear();
        ((BridgePlanToEncode) menu).eaep$plan();

        recipe = EmiRecipeAdaptable.of(recipe, node, recipe.isNonProcessing() ? 1 : BoM.tree.batches);
        EmiRecipeFiller.performFill(recipe,
                this.old,
                EmiCraftContext.Type.FILL_BUTTON,
                EmiCraftContext.Destination.NONE,
                1);

        player.playSound(SoundEvents.UI_BUTTON_CLICK.value());

        cir.setReturnValue(true);
    }
}
