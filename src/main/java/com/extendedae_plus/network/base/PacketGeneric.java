package com.extendedae_plus.network.base;

import com.extendedae_plus.common.init.ModNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface PacketGeneric extends CustomPacketPayload {
    StreamCodec<RegistryFriendlyByteBuf, ? extends PacketGeneric> STREAM_CODEC = null;

    @Override
    default Type<? extends PacketGeneric> type() {
        var typeRegistered = ModNetwork.getType(this.getClass().getSimpleName());
        if (typeRegistered == null)
            throw new IllegalStateException("Unknown NetworkPacket:" + this.getClass().getSimpleName());
        return typeRegistered;
    }
}
