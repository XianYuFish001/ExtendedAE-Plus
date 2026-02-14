package com.extendedae_plus.network.base;

import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface SPacketGeneric extends PacketGeneric {
    @OnlyIn(Dist.CLIENT)
    void handleClient(final LocalPlayer player);

    default void handle(final IPayloadContext context) {
        if (context.player() instanceof LocalPlayer player)
            this.handleClient(player);
    }
}
