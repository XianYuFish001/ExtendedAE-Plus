package com.extendedae_plus.network;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.common.impl.pattern.PatternUploader;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

@EAEPNetworkPacket
public record CPacketUploadTerminalPattern(int serverID) implements CPacketGeneric {
    public static final Type<CPacketUploadTerminalPattern> TYPE = PacketGeneric.createType("upload_terminal_pattern");

    public static final StreamCodec<FriendlyByteBuf, CPacketUploadTerminalPattern> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CPacketUploadTerminalPattern::serverID,
            CPacketUploadTerminalPattern::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof PatternEncodingTermMenu menu)) return;

        PatternUploader.uploadToProvider(menu, this.serverID);
    }
}
