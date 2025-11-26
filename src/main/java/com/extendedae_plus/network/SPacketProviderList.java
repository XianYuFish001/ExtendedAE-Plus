package com.extendedae_plus.network;

import com.extendedae_plus.client.screen.ProviderListScreen;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import com.extendedae_plus.network.base.SPacketGeneric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * S2C: 返回可见且有空位的样板供应器列表，客户端弹窗展示供用户选择。
 */
@EAEPNetworkPacket
public record SPacketProviderList(List<Long> ids, List<String> names, List<String> i18nKeys,
                                  List<Integer> emptySlots) implements SPacketGeneric {
    public static final Type<SPacketProviderList> TYPE = PacketGeneric.createType("provider_list");

    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketProviderList> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), SPacketProviderList::ids,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SPacketProviderList::names,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SPacketProviderList::i18nKeys,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), SPacketProviderList::emptySlots,
            SPacketProviderList::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleClient(final LocalPlayer player) {
        var mc = Minecraft.getInstance();
        var current = mc.screen;
        mc.setScreen(new ProviderListScreen(current, this.ids, this.names, this.i18nKeys, this.emptySlots));
    }
}
