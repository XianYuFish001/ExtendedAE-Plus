package com.extendedae_plus.mixin.core.ae2.menu;

import appeng.menu.AEBaseMenu;
import com.extendedae_plus.util.UtilGson;
import com.google.gson.Gson;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AEBaseMenu.class)
public class MixinBaseMenu {
    @Mixin(targets = "appeng.menu.AEBaseMenu$ClientAction")
    public static class MixinClientAction {
        @Shadow
        @Final
        @Mutable
        private Gson gson;

        @Inject(method = "<init>", at = @At("TAIL"))
        private void onInit(CallbackInfo ci) {
            this.gson = this.gson.newBuilder()
                    .registerTypeAdapter(Component.class, UtilGson.adapterComponent)
                    .create();
        }
    }
}
