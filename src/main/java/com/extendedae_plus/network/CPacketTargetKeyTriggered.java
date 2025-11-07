package com.extendedae_plus.network;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.mixin.impl.bridge.BridgeCtrlPressed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CPacketTargetKeyTriggered(KeyType keyType) implements CustomPacketPayload{
    public enum KeyType {
        CTRL_DOWN,
        CTRL_UP
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final Type<CPacketTargetKeyTriggered> TYPE = new Type<>(
            ExtendedAEPlus.getLocation("target_key_pressed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketTargetKeyTriggered> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(KeyType.class), CPacketTargetKeyTriggered::keyType,
            CPacketTargetKeyTriggered::new);

    public static void handle(CPacketTargetKeyTriggered packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().isClientSide) return;
            ServerPlayer player = (ServerPlayer) context.player();

            if (player.containerMenu instanceof PatternEncodingTermMenu patternMenu) {
                if (patternMenu instanceof BridgeCtrlPressed accessor) {
                    accessor.eaep$setCtrlPressed(switch (packet.keyType) {
                        case CTRL_UP -> false;
                        case CTRL_DOWN -> true;
                    });
                }
            }
        });
    }
}
