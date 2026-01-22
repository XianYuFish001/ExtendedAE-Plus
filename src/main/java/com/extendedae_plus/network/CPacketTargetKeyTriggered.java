package com.extendedae_plus.network;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.mixin.impl.bridge.BridgeCtrlPressed;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

@EAEPNetworkPacket("target_key_pressed")
public record CPacketTargetKeyTriggered(KeyType keyType) implements CPacketGeneric {
    public enum KeyType {
        CTRL_DOWN,
        CTRL_UP
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketTargetKeyTriggered> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(KeyType.class), CPacketTargetKeyTriggered::keyType,
            CPacketTargetKeyTriggered::new
    );

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
