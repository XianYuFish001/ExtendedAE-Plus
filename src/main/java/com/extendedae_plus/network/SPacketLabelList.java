package com.extendedae_plus.network;

import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink;
import com.extendedae_plus.common.wireless.linkApi.RegistryLink;
import com.extendedae_plus.integration.IntegrationFTBTeams;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.SPacketGeneric;
import com.extendedae_plus.network.helper.HelperHandlerClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Objects;

@EAEPNetworkPacket("label_list")
public record SPacketLabelList(List<MenuLabelLink.LabelMapped> labels) implements SPacketGeneric {
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketLabelList> STREAM_CODEC = StreamCodec.composite(
            MenuLabelLink.LabelMapped.STREAM_CODEC.apply(ByteBufCodecs.list()), SPacketLabelList::labels,
            SPacketLabelList::new
    );

    public static void send(MenuLabelLink menu) {
        var placer = IntegrationFTBTeams.instance
                .getTeamUUID(menu.getPlayer().getUUID())
                .orElse(menu.getPlayer().getUUID());

        var serial = new int[]{Integer.MIN_VALUE};
        var labels = RegistryLink.getLabels().stream()
                .filter(label -> Objects.equals(label.data.placer(), placer) || label.data.placer() == null)
                .map(label -> new MenuLabelLink.LabelMapped(serial[0]++, label.data))
                .toList();
        menu.setLabels(labels);
        PacketDistributor.sendToPlayer((ServerPlayer) menu.getPlayer(), new SPacketLabelList(labels));
    }

    @Override
    public void handleClient(LocalPlayer player) {
        HelperHandlerClient.instance.labelList(this, player);
    }
}