package com.extendedae_plus.network;

import com.extendedae_plus.client.screen.ProviderSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * S2C: 返回可见且有空位的样板供应器列表，客户端弹窗展示供用户选择。
 */
public record ProvidersListS2CPacket(List<Long> ids, List<String> names, List<String> i18nKeys,
                                     List<Integer> emptySlots) {

    public static void encode(ProvidersListS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.ids.size());
        for (int i = 0; i < msg.ids.size(); i++) {
            buf.writeLong(msg.ids.get(i));
            buf.writeUtf(msg.names.get(i));
            buf.writeUtf(msg.i18nKeys.get(i));
            buf.writeVarInt(msg.emptySlots.get(i));
        }
    }

    public static ProvidersListS2CPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Long> ids = new ArrayList<>(size);
        List<String> names = new ArrayList<>(size);
        List<String> i18nKeys = new ArrayList<>(size);
        List<Integer> slots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readLong());
            names.add(buf.readUtf());
            i18nKeys.add(buf.readUtf());
            slots.add(buf.readVarInt());
        }
        return new ProvidersListS2CPacket(ids, names, i18nKeys, slots);
    }

    public static void handle(ProvidersListS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> handleClient(msg));
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(ProvidersListS2CPacket msg) {
        var mc = Minecraft.getInstance();
        var current = mc.screen;
        mc.setScreen(new ProviderSelectScreen(current, msg.ids, msg.names, msg.i18nKeys, msg.emptySlots));
    }
}
