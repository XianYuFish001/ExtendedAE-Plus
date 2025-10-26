package com.extendedae_plus.network;

import com.extendedae_plus.init.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CPacketEncodeFinished() {
    public static void encode(S2CPacketEncodeFinished msg, FriendlyByteBuf buf) {}

    public static S2CPacketEncodeFinished decode(FriendlyByteBuf buf) { return new S2CPacketEncodeFinished(); }

    public static final S2CPacketEncodeFinished INSTANCE = new S2CPacketEncodeFinished();

    public static void handle(final S2CPacketEncodeFinished packet, final Supplier<NetworkEvent.Context> context) {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.isClientSide)
            ModNetwork.CHANNEL.sendToServer(new RequestProvidersListC2SPacket());
        context.get().setPacketHandled(true);
    }
}
