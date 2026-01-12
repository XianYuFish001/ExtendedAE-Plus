package com.extendedae_plus.network;

import com.extendedae_plus.common.impl.pattern.InfoProvider;
import com.extendedae_plus.mixin.impl.bridge.BridgeProviderList;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

/**
 * C2S: 请求当前终端可见的样板供应器列表（用于弹窗选择）。
 */
@EAEPNetworkPacket
public class CPacketRequestUploading implements CPacketGeneric {
    public static final Type<CPacketRequestUploading> TYPE = PacketGeneric.createType("request_uploading");

    public static final CPacketRequestUploading INSTANCE = new CPacketRequestUploading();

    public static final StreamCodec<FriendlyByteBuf, CPacketRequestUploading> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof BridgeProviderList helper)) return;

        var providers = helper.eaep$getProviderList();
        if (providers.isEmpty()) return;

        var info = new ArrayList<InfoProvider>();
        providers.forEach((group, containers) -> {
            var availableSlots = 0;
            for (var container : containers) {
                var inv = container.getTerminalPatternInventory();
                for (int indexStack = 0; indexStack < inv.size(); indexStack++) {
                    if (inv.getStackInSlot(indexStack).isEmpty())
                        availableSlots++;
                }
            }
            info.add(new InfoProvider(group.name(), group.icon(), group.hashCode(), availableSlots));
        });

        PacketDistributor.sendToPlayer(player, new SPacketProvidersInfo(info));
    }
}
