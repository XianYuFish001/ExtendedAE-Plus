package com.extendedae_plus.network;

import appeng.util.EnumCycler;
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority;
import com.extendedae_plus.common.registry.menu.MenuPriorityTool;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EAEPNetworkPacket("priority_tool_operation")
public record CPacketPriorityToolOperation(@Nullable Integer priority, boolean rotateMode) implements CPacketGeneric {
    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketPriorityToolOperation> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(ByteBufCodecs.INT), data ->
                            Optional.ofNullable(data.priority),
                    ByteBufCodecs.BOOL, CPacketPriorityToolOperation::rotateMode,
                    (priority, rotateMode) ->
                            new CPacketPriorityToolOperation(priority.orElse(null), rotateMode));

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
