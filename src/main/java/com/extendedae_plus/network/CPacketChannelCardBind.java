package com.extendedae_plus.network;

import com.extendedae_plus.common.dataComponent.DataChannelCard;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.extendedae_plus.util.WirelessTeamUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.UUID;

/**
 * 频道卡绑定网络包
 * 客户端发送到服务端，用于处理左键空气的绑定/解绑操作
 */
@EAEPNetworkPacket
public record CPacketChannelCardBind(InteractionHand hand) implements CPacketGeneric {
    public static final Type<CPacketChannelCardBind> TYPE = PacketGeneric.createType("channel_card_bind");

    public static final StreamCodec<FriendlyByteBuf, CPacketChannelCardBind> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), CPacketChannelCardBind::hand,
            CPacketChannelCardBind::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleServer(final ServerPlayer player) {
        ItemStack stack = player.getItemInHand(this.hand);
        if (stack.getItem() != ModItems.CHANNEL_CARD.get()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        UUID currentOwner = DataChannelCard.getOwnerUUID(stack);

        if (currentOwner != null) {
            // 已有所有者，清除
            DataChannelCard.clearOwner(stack);
            player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.actionBar)
                            .item(ModItems.CHANNEL_CARD)
                            .addStr("binding")
                            .addStr("clear")
                            .build(),
                    true
            );
        } else {
            // 写入当前玩家的UUID和团队信息
            UUID playerUUID = player.getUUID();
            DataChannelCard.setOwnerUUID(stack, playerUUID);

            // 获取团队名称用于显示
            Component teamName = WirelessTeamUtil.getNetworkOwnerName(level, playerUUID);
            DataChannelCard.setOwnerName(stack, teamName.getString());

            player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.actionBar)
                            .item(ModItems.CHANNEL_CARD)
                            .addStr("binding")
                            .args(teamName.getString())
                            .build(),
                    true
            );
        }
    }
}

