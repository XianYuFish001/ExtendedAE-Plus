package com.extendedae_plus.network;

import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import com.extendedae_plus.common.impl.pattern.PatternUploader;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

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
        if (!(player.containerMenu instanceof PatternEncodingTermMenu encMenu)) return;

        if (encMenu.getMode() != EncodingMode.PROCESSING) {
            try {
                PatternUploader.uploadFromEncodingMenuToMatrix(player, encMenu);
            } catch (Throwable ignored) {}
            return;
        }

        // 优先：若玩家也打开了样板访问终端，则用 byId 方式（精确服务器ID）
        PatternAccessTermMenu accessMenu = PatternUploader.getPatternAccessMenu(player);
        if (accessMenu != null) {
            List<Long> ids = PatternUploader.getAllProviderIds(accessMenu);
            List<Long> filteredIds = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<String> i18nKeys = new ArrayList<>();
            List<Integer> slots = new ArrayList<>();

            for (Long id : ids) {
                if (id == null) continue;
                if (!PatternUploader.isProviderAvailable(id, accessMenu)) continue;
                int empty = PatternUploader.getAvailableSlots(id, accessMenu);
                if (empty <= 0) continue; // 只列出有空位的
                filteredIds.add(id);
                names.add(PatternUploader.getProviderDisplayName(id, accessMenu));
                i18nKeys.add(PatternUploader.getProviderI18nName(id, accessMenu));
                slots.add(empty);
            }

            PacketDistributor.sendToPlayer(player, new SPacketProviderList(filteredIds, names, i18nKeys, slots));
        } else {
            // 回退：基于编码终端所在网络枚举供应器，用“负数ID编码索引”：encodedId = -1 - index
            List<PatternContainer> containers = PatternUploader.listAvailableProvidersFromGrid(encMenu);
            List<Long> idxIds = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<String> i18nKeys = new ArrayList<>();
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < containers.size(); i++) {
                var c = containers.get(i);
                if (c == null) continue;
                int empty = PatternUploader.getAvailableSlots(c);
                if (empty <= 0) continue;
                long encodedId = -1L - i; // 约定：负数代表按索引
                idxIds.add(encodedId);
                names.add(PatternUploader.getProviderDisplayName(c));
                i18nKeys.add(PatternUploader.getProviderI18nName(c));
                slots.add(empty);
            }

            PacketDistributor.sendToPlayer(player, new SPacketProviderList(idxIds, names, i18nKeys, slots));
        }
    }
}
