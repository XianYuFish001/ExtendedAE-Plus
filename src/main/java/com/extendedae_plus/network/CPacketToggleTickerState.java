package com.extendedae_plus.network;

import com.extendedae_plus.common.menu.EntitySpeedTickerMenu;
import com.extendedae_plus.common.part.EntitySpeedTickerPart;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * C2S: Toggle the accelerateEnabled flag on the EntitySpeedTickerPart bound to the open menu.
 */
@EAEPNetworkPacket
public class CPacketToggleTickerState implements CPacketGeneric {
    public static final CustomPacketPayload.Type<CPacketToggleTickerState> TYPE =
            PacketGeneric.createType("toggle_ticker_state");

    public static final CPacketToggleTickerState INSTANCE = new CPacketToggleTickerState();

    public static final StreamCodec<FriendlyByteBuf, CPacketToggleTickerState> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof EntitySpeedTickerMenu menu)) return;

        EntitySpeedTickerPart part = menu.getHost();
        if (part == null) return;

        // 切换部件上的状态，并把新状态同步到菜单字段，随后广播以通知客户端
        boolean current = part.getAccelerateEnabled();
        boolean next = !current;
        part.setAccelerateEnabled(next);
        // 确保菜单上的字段也被更新，这样 @GuiSync 会把状态发回客户端
        menu.setAccelerateEnabled(next);
        menu.broadcastChanges();
    }
}