package com.extendedae_plus.mixin.core.recipeViewer.emi;

import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.bridge.BridgePlanToEncode;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import dev.emi.emi.widget.RecipeButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MixinDependencies("emi")
@Mixin(RecipeFillButtonWidget.class)
public class MixinRecipeFillButton extends RecipeButtonWidget {
    @Shadow
    private boolean canFill;

    public MixinRecipeFillButton(int x, int y, int u, int v, EmiRecipe recipe) {
        super(x, y, u, v, recipe);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void eaep$onMouseClicked(int mouseX, int mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!this.canFill) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.containerMenu instanceof BridgePlanToEncode bridge)) return;
        if (!Screen.hasControlDown()) return;
        if (VanillaEmiRecipeCategories.STONECUTTING.equals(this.recipe.getCategory())) return;
        bridge.eaep$plan();
    }
}
