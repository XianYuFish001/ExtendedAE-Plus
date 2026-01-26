package com.extendedae_plus.mixin.core.minecraft;

import com.extendedae_plus.util.UtilTextComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinComponentsClick {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style == null || !(style.getClickEvent() instanceof UtilTextComponent.ClickEventCustomizable event)) return;
        event.trigger();
        cir.setReturnValue(true);
    }
}
