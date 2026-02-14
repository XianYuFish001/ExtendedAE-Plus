package com.extendedae_plus.network;

import com.extendedae_plus.mixin.bridge.BridgeProviderList;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/**
 * C2S: 请求当前终端可见的样板供应器列表（用于弹窗选择）。
 */
@EAEPNetworkPacket("request_uploading")
public class CPacketRequestUploading implements CPacketGeneric {
    public static final CPacketRequestUploading INSTANCE = new CPacketRequestUploading();

    public static final StreamCodec<FriendlyByteBuf, CPacketRequestUploading> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof BridgeProviderList helper)) return;
        SPacketProvidersInfo.send(player, helper);
    }
}
