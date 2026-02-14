package com.extendedae_plus.network;

import com.extendedae_plus.common.impl.pattern.InfoProvider;
import com.extendedae_plus.mixin.bridge.BridgeProviderList;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.SPacketGeneric;
import com.extendedae_plus.network.helper.HelperHandlerClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EAEPNetworkPacket("provider_info")
public record SPacketProvidersInfo(List<InfoProvider> info) implements SPacketGeneric {
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketProvidersInfo> STREAM_CODEC = StreamCodec.composite(
            InfoProvider.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SPacketProvidersInfo::info,
            SPacketProvidersInfo::new
    );

    public static void send(ServerPlayer player, BridgeProviderList helper) {
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

    @Override
    public void handleClient(final LocalPlayer player) {
        HelperHandlerClient.instance.providersInfo(this, player);
    }
}
