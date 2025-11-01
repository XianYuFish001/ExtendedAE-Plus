package com.extendedae_plus.network;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CPacketStoneCuttingID(ResourceLocation recipeID) implements CustomPacketPayload {
    public static final Type<CPacketStoneCuttingID> TYPE = new Type<>(
            ExtendedAEPlus.getLocation("bom_pattern_encoding"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketStoneCuttingID> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CPacketStoneCuttingID::recipeID,
            CPacketStoneCuttingID::new);

    public static void handle(CPacketStoneCuttingID packet, IPayloadContext context) {
        if (context.player().level().isClientSide) return;
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof PatternEncodingTermMenu menu)) return;
            menu.setStonecuttingRecipeId(packet.recipeID);
        });
    }
}
