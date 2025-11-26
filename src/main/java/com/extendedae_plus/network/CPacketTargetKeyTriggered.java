package com.extendedae_plus.network;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.mixin.impl.bridge.BridgeCtrlPressed;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

@EAEPNetworkPacket
public record CPacketTargetKeyTriggered(KeyType keyType) implements CPacketGeneric {
    public enum KeyType {
        CTRL_DOWN,
        CTRL_UP
    }

    public static final Type<CPacketTargetKeyTriggered> TYPE = PacketGeneric.createType("target_key_pressed");

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketTargetKeyTriggered> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(KeyType.class), CPacketTargetKeyTriggered::keyType,
            CPacketTargetKeyTriggered::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (player.containerMenu instanceof PatternEncodingTermMenu patternMenu) {
            if (patternMenu instanceof BridgeCtrlPressed accessor) {
                accessor.eaep$setCtrlPressed(switch (this.keyType) {
                    case CTRL_UP -> false;
                    case CTRL_DOWN -> true;
                });
            }
        }
    }
}
