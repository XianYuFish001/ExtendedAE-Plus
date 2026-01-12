package com.extendedae_plus.mixin.core.ae2;

import appeng.menu.guisync.SynchronizedField;
import com.extendedae_plus.common.impl.guiSync.PacketStreamable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(SynchronizedField.class)
public class MixinSyncedField {
    @Mixin(targets = "appeng.menu.guisync.SynchronizedField$CustomField")
    public static class MixinFieldCustom {
        @Shadow
        @Final
        private Class<?> fieldType;

        @SuppressWarnings("rawtypes")
        @Unique
        private @Nullable StreamCodec eaep$streamCodec = null;

        @SuppressWarnings("unchecked")
        @Inject(method = "<init>", at = @At("TAIL"))
        private void onInit(Object source, Field field, CallbackInfo ci) {
            if (!PacketStreamable.class.isAssignableFrom(this.fieldType)) return;
            this.eaep$streamCodec = PacketStreamable.getStreamCodec((Class<? extends PacketStreamable>) this.fieldType);
        }

        @SuppressWarnings("unchecked")
        @Inject(method = "writeValue", at = @At("HEAD"), cancellable = true)
        private void writeValue(RegistryFriendlyByteBuf data, Object value, CallbackInfo ci) {
            if (this.eaep$streamCodec == null) return;
            this.eaep$streamCodec.encode(data, value);
            ci.cancel();
        }

        @SuppressWarnings("unchecked")
        @Inject(method = "readValue", at = @At("HEAD"), cancellable = true)
        private void readValue(RegistryFriendlyByteBuf data, CallbackInfoReturnable<Object> cir) {
            if (this.eaep$streamCodec == null) return;
            cir.setReturnValue(this.eaep$streamCodec.decode(data));
        }
    }
}
