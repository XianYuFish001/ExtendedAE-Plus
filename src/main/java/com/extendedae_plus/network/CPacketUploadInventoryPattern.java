package com.extendedae_plus.network;

import com.extendedae_plus.common.impl.pattern.PatternUploader;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/**
 * C2S: 从样板访问终端上传玩家背包中的编码样板到指定的样板供应器。
 * 适用于 ExtendedAE 的 GuiExPatternTerminal 或 AE2 的 PatternAccessTermScreen。
 */
@EAEPNetworkPacket("upload_inventory_pattern")
public record CPacketUploadInventoryPattern(int playerSlotIndex,
                                            long providerId) implements CPacketGeneric {
    public static final StreamCodec<FriendlyByteBuf, CPacketUploadInventoryPattern> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CPacketUploadInventoryPattern::playerSlotIndex,
            ByteBufCodecs.VAR_LONG, CPacketUploadInventoryPattern::providerId,
            CPacketUploadInventoryPattern::new
    );

    @Override
    public void handleServer(ServerPlayer player) {
        PatternUploader.uploadToProvider(player, this.playerSlotIndex, this.providerId);
    }
}
