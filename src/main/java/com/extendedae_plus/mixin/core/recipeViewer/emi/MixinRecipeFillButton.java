package com.extendedae_plus.mixin.core.recipeViewer.emi;

import com.extendedae_plus.mixin.impl.bridge.BridgePlanToEncode;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeFillButtonWidget.class)
public class MixinRecipeFillButton {

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void eaep$onMouseClicked(int mouseX, int mouseY, int button,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.containerMenu instanceof BridgePlanToEncode bridge)) return;
        if (!Screen.hasControlDown()) return;
        bridge.eaep$plan();
    }
}
