package com.extendedae_plus.common.impl.guiSync;

import appeng.menu.guisync.PacketWritable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface PacketStreamable extends PacketWritable {
    @SuppressWarnings("rawtypes")
    static <TField extends PacketStreamable> StreamCodec
    getStreamCodec(Class<TField> clazzField) {
        try {
            return (StreamCodec) clazzField
                    .getField("STREAM_CODEC")
                    .get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("Cannot get StreamCodec");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    default void writeToPacket(RegistryFriendlyByteBuf data) {
        PacketStreamable.getStreamCodec(this.getClass()).encode(data, this);
    }
}
