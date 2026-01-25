package com.extendedae_plus.mixin.core.recipeViewer.emi;

import appeng.client.gui.me.items.PatternEncodingTermScreen;
import com.extendedae_plus.mixin.impl.bridge.BridgePlanToEncode;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiRecipeFiller.class)
public class MixinRecipeFiller {
    @Inject(method = "performFill", at = @At("RETURN"))
    private static void performFill(EmiRecipe recipe,
                                    AbstractContainerScreen<?> screen,
                                    EmiCraftContext.Type type,
                                    EmiCraftContext.Destination destination,
                                    int amount,
                                    CallbackInfoReturnable<Boolean> cir) {
        if (!(screen instanceof PatternEncodingTermScreen<?> screenEncode)) return;
        var menu = screenEncode.getMenu();
        if (!(menu instanceof BridgePlanToEncode helper)) return;
        if (!helper.eaep$planned()) return;
        menu.encode();
    }
}
