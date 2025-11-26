package com.extendedae_plus.network;

import appeng.api.stacks.AEKey;
import com.extendedae_plus.client.ClientPatternHighlightStore;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import com.extendedae_plus.network.base.SPacketGeneric;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C: 指示客户端对某个 AEKey 的样板进行高亮/取消高亮（仅作用于接收该包的客户端）。
 * 使用 NeoForge 1.21 Payload API。
 */
@EAEPNetworkPacket
public record SPacketHighlightPatternSlot(AEKey key, boolean highlight) implements SPacketGeneric {
    public static final Type<SPacketHighlightPatternSlot> TYPE = PacketGeneric.createType("highlight_pattern_slot");

    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketHighlightPatternSlot> STREAM_CODEC = StreamCodec.composite(
            AEKey.STREAM_CODEC, SPacketHighlightPatternSlot::key,
            ByteBufCodecs.BOOL, SPacketHighlightPatternSlot::highlight,
            SPacketHighlightPatternSlot::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleClient(LocalPlayer player) {
        try {
            ClientPatternHighlightStore.setHighlight(this.key, this.highlight);
        } catch (Throwable ignored) {
        }
    }
}


