package com.extendedae_plus.network;

import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.SPacketGeneric;
import com.extendedae_plus.network.helper.HelperHandlerClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * S2C: 指示客户端在已打开的样板供应器界面切换到指定页
 */
@EAEPNetworkPacket("set_provider_page")
public record SPacketSetProviderPage(int page) implements SPacketGeneric {
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSetProviderPage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SPacketSetProviderPage::page,
            SPacketSetProviderPage::new
    );

    @Override
    public void handleClient(LocalPlayer player) {
        HelperHandlerClient.instance.setProviderPage(this, player);
    }
}


