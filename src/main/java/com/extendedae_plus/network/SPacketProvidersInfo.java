package com.extendedae_plus.network;

import appeng.client.gui.me.items.PatternEncodingTermScreen;
import com.extendedae_plus.client.screen.ScreenProviderList;
import com.extendedae_plus.common.impl.pattern.InfoProvider;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.SPacketGeneric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

@EAEPNetworkPacket("provider_info")
public record SPacketProvidersInfo(List<InfoProvider> info) implements SPacketGeneric {
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketProvidersInfo> STREAM_CODEC = StreamCodec.composite(
            InfoProvider.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SPacketProvidersInfo::info,
            SPacketProvidersInfo::new
    );

    @Override
    public void handleClient(final LocalPlayer player) {
        if (!(Minecraft.getInstance().screen instanceof PatternEncodingTermScreen<?> screen)) return;
        screen.switchToScreen(new ScreenProviderList<>(screen, this.info));
    }
}
