package com.extendedae_plus.network.base;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface CPacketGeneric extends PacketGeneric {
    void handleServer(final ServerPlayer player);

    default void handle(final IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player)
            this.handleServer(player);
    }
}
