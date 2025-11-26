package com.extendedae_plus.network;

import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

/**
 * C2S: 客户端发送到服务端，用于设置无线收发器的频率
 */
@EAEPNetworkPacket
public record CPacketSetTransceiverFrequency(BlockPos pos, long frequency) implements CPacketGeneric {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<CPacketSetTransceiverFrequency> TYPE = PacketGeneric.createType("set_transceiver_frequency");

    public static final StreamCodec<FriendlyByteBuf, CPacketSetTransceiverFrequency> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CPacketSetTransceiverFrequency::pos,
            ByteBufCodecs.VAR_LONG, CPacketSetTransceiverFrequency::frequency,
            CPacketSetTransceiverFrequency::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        // 验证玩家是否在附近（防止作弊）
        if (player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) > 64.0) {
            LOGGER.warn("Player {} tried to set frequency from too far away", player.getName().getString());
            return;
        }

        // 获取方块实体
        BlockEntity be = player.level().getBlockEntity(this.pos);
        if (!(be instanceof BlockEntityWirelessTransceiver transceiver)) {
            LOGGER.warn("Invalid block entity at {} for frequency setting", this.pos);
            return;
        }

        // 使用强制设置方法，忽略锁定状态
        // 扳手GUI调整频率时应该能够绕过锁定限制
        transceiver.setFrequency(this.frequency, true);
        LOGGER.debug("Set transceiver frequency at {} to {} (forced)", this.pos, this.frequency);
    }
}

