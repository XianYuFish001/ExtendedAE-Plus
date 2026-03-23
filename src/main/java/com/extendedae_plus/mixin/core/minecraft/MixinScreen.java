package com.extendedae_plus.mixin.core.minecraft;

import com.extendedae_plus.mixin.event.EventScreen;
import com.fish.fishlib.util.extension.ExtensionStdKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen implements EventScreen.AccessorEvent {
    @Unique
    private Function1<Screen, Unit> eaep$ticker = $ -> Unit.INSTANCE;


    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        this.eaep$ticker.invoke(ExtensionStdKt.cast(this));
    }


    @Override
    public Function1<Screen, Unit> eaep$ticker() {
        return this.eaep$ticker;
    }
    @Override
    public void eaep$ticker(Function1<Screen, Unit> ticker) {
        this.eaep$ticker = ticker;
    }
}
