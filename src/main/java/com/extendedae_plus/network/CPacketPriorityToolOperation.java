package com.extendedae_plus.network;

import appeng.util.EnumCycler;
import com.extendedae_plus.common.item.priorityTool.DataPriority;
import com.extendedae_plus.common.menu.MenuPriorityTool;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

@EAEPNetworkPacket
public record CPacketPriorityToolOperation(int priority, boolean rotate) implements CPacketGeneric {
    public static final Type<CPacketPriorityToolOperation> TYPE =
            PacketGeneric.createType("priority_tool_operation");

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketPriorityToolOperation> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, CPacketPriorityToolOperation::priority,
                    ByteBufCodecs.BOOL, CPacketPriorityToolOperation::rotate,
                    CPacketPriorityToolOperation::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof MenuPriorityTool menu)) return;
        var data = menu.getData();
        menu.setData(this.rotate
                ? new DataPriority(data.priority(), EnumCycler.next(data.modeTool()))
                : new DataPriority(this.priority, data.modeTool()));
    }
}
