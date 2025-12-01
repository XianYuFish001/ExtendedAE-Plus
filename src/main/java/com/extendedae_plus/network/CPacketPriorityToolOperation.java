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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EAEPNetworkPacket
public record CPacketPriorityToolOperation(@Nullable Integer priority, boolean rotateMode) implements CPacketGeneric {
    public static final Type<CPacketPriorityToolOperation> TYPE =
            PacketGeneric.createType("priority_tool_operation");

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketPriorityToolOperation> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(ByteBufCodecs.INT), data ->
                            Optional.ofNullable(data.priority),
                    ByteBufCodecs.BOOL, CPacketPriorityToolOperation::rotateMode,
                    (priority, rotateMode) ->
                            new CPacketPriorityToolOperation(priority.orElse(null), rotateMode));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof MenuPriorityTool menu)) return;
        var data = menu.getData();
        var newData = new DataPriority(
                this.priority == null ? data.priority() : this.priority,
                this.rotateMode ? EnumCycler.next(data.modeTool()) : data.modeTool()
        );
        menu.setData(newData);
    }
}
