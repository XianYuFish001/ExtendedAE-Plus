package com.extendedae_plus.common.impl.guiSync;

import appeng.menu.guisync.PacketWritable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/// @see com.extendedae_plus.mixin.core.ae2.MixinSyncedField.MixinFieldCustom
public interface PacketStreamable extends PacketWritable {
    Map<Class<? extends PacketStreamable>, StreamCodec<RegistryFriendlyByteBuf, ? extends PacketStreamable>> registry = new HashMap<>();

    static <TField extends PacketStreamable> void register(
            Class<TField> clazzField, StreamCodec<RegistryFriendlyByteBuf, TField> streamCodec) {
        if (registry.put(clazzField, streamCodec) != null)
            throw new IllegalStateException("Duplicate field found for " + clazzField.getSimpleName());
    }

    @SuppressWarnings("rawtypes")
    static Optional<StreamCodec> getStreamCodec(Class<?> clazzField) {
        var streamCodec = registry.get(clazzField);
        if (PacketStreamable.class.isAssignableFrom(clazzField) && streamCodec == null)
            throw new IllegalStateException("Unregistered streamable object: " + clazzField.getSimpleName());
        return Optional.ofNullable(streamCodec);
    }

    @SuppressWarnings("unchecked")
    @Override
    default void writeToPacket(RegistryFriendlyByteBuf data) {
        PacketStreamable.getStreamCodec(this.getClass())
                .ifPresent(streamCodec -> streamCodec.encode(data, this));
    }
}
