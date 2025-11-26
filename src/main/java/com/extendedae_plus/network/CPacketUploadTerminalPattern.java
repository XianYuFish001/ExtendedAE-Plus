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

/**
 * C2S: 请求将图样编码终端的已编码样板上传到指定的样板供应器（由客户端选择）。
 */
@EAEPNetworkPacket
public record CPacketUploadTerminalPattern(long providerId) implements CPacketGeneric {
    public static final Type<CPacketUploadTerminalPattern> TYPE = PacketGeneric.createType("upload_terminal_pattern");

    public static final StreamCodec<FriendlyByteBuf, CPacketUploadTerminalPattern> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, CPacketUploadTerminalPattern::providerId,
            CPacketUploadTerminalPattern::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof PatternEncodingTermMenu menu)) return;
        // 支持两种模式：
        // 1) providerId >= 0: 访问终端 byId 模式
        // 2) providerId < 0:   索引模式（由列表回退路径生成），index = -1 - providerId
        if (this.providerId >= 0) {
            PatternUploader.uploadFromEncodingMenuToProvider(player, menu, this.providerId);
        } else {
            int index = (int) (-1L - this.providerId);
            PatternUploader.uploadFromEncodingMenuToProviderByIndex(player, menu, index);
        }
    }
}
