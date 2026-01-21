package com.extendedae_plus.mixin.core.minecraft;

import com.extendedae_plus.mixin.impl.bridge.HelperComponentColorful;
import com.extendedae_plus.util.UtilTextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MutableComponent.class)
public class MixinComponent implements HelperComponentColorful {
    @Unique
    private @Nullable UtilTextComponent.ComponentColorful eaep$colored = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(ComponentContents contents, List<Component> siblings, Style style, CallbackInfo ci) {
        if (!(contents instanceof TranslatableContents contentsTranslatable)) return;
        var colored = UtilTextComponent.RegistryColored.find(contentsTranslatable.getKey());
        if (colored.isEmpty()) return;
        this.eaep$colored = colored.get();
    }

    @Inject(method = "append(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("RETURN"))
    private void onAppend(Component sibling, CallbackInfoReturnable<MutableComponent> cir) {
        if (!(sibling instanceof HelperComponentColorful helper)) return;
        var colored = helper.eaep$getColored();
        if (colored == null) return;
        helper.eaep$clearColored();
        this.eaep$colored = new UtilTextComponent.ComponentColorful((MutableComponent)(Object) this);
    }

    @Inject(method = "getContents", at = @At("HEAD"), cancellable = true)
    private void getContents(CallbackInfoReturnable<ComponentContents> cir) {
        if (this.eaep$colored == null) return;
        cir.setReturnValue(this.eaep$colored.getContents());
    }

    @Inject(method = "getSiblings", at = @At("HEAD"), cancellable = true)
    private void getSiblings(CallbackInfoReturnable<List<Component>> cir) {
        if (this.eaep$colored == null) return;
        cir.setReturnValue(this.eaep$colored.getSiblings());
    }

    @Inject(method = "getStyle", at = @At("HEAD"), cancellable = true)
    private void getStyle(CallbackInfoReturnable<Style> cir) {
        if (this.eaep$colored == null) return;
        cir.setReturnValue(this.eaep$colored.getStyle());
    }

    @Inject(method = "getVisualOrderText", at = @At("HEAD"), cancellable = true)
    private void getVisualOrderText(CallbackInfoReturnable<FormattedCharSequence> cir) {
        if (this.eaep$colored == null) return;
        cir.setReturnValue(this.eaep$colored.getVisualOrderText());
    }

    @Override
    public @Nullable UtilTextComponent.ComponentColorful eaep$getColored() {
        return this.eaep$colored;
    }

    @Override
    public void eaep$clearColored() {
        this.eaep$colored = null;
    }
}
