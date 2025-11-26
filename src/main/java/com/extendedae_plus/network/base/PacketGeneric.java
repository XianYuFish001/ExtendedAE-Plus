package com.extendedae_plus.network.base;

import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface PacketGeneric extends CustomPacketPayload {
    Type<?> TYPE = createType("empty");
    StreamCodec<RegistryFriendlyByteBuf, PacketGeneric> STREAM_CODEC = null;

    static <TPacket extends PacketGeneric> Type<TPacket> createType(String path) {
        return new Type<>(ExtendedAEPlus.getLocation(path));
    }
}
