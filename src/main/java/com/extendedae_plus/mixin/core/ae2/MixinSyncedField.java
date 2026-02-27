package com.extendedae_plus.mixin.core.ae2;

import appeng.menu.guisync.SynchronizedField;
import com.extendedae_plus.common.impl.guiSync.PacketStreamable;
import kotlin.jvm.JvmClassMappingKt;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SynchronizedField.class)
public class MixinSyncedField {
    @Mixin(targets = "appeng.menu.guisync.SynchronizedField$CustomField")
    public static class MixinFieldCustom {
        @Shadow
        @Final
        private Class<?> fieldType;

        @Inject(method = "writeValue", at = @At("HEAD"), cancellable = true)
        private void writeValue(RegistryFriendlyByteBuf data, Object value, CallbackInfo ci) {
            var streamCodec =
                    PacketStreamable.getStreamCodec(JvmClassMappingKt.getKotlinClass(this.fieldType));
            if (streamCodec == null) return;
            streamCodec.encode(data, value);
            ci.cancel();
        }

        @Inject(method = "readValue", at = @At("HEAD"), cancellable = true)
        private void readValue(RegistryFriendlyByteBuf data, CallbackInfoReturnable<Object> cir) {
            var streamCodec =
                    PacketStreamable.getStreamCodec(JvmClassMappingKt.getKotlinClass(this.fieldType));
            if (streamCodec == null) return;
            cir.setReturnValue(streamCodec.decode(data));
        }
    }
}
