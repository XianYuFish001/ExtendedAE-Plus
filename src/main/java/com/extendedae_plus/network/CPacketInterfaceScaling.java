package com.extendedae_plus.network;

import appeng.api.stacks.GenericStack;
import appeng.menu.implementations.InterfaceMenu;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.glodblock.github.extendedae.container.ContainerExInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S：调整 ME 接口配置槽位(标记物品)的数量。
 * 支持按因子倍增或整除，且保持最小值为 1。
 */
public record CPacketInterfaceScaling(int scale, boolean divide) implements CustomPacketPayload {
    public static final Type<CPacketInterfaceScaling> TYPE = new Type<>(
            ExtendedAEPlus.getLocation("interface_adjust_config_amount"));

    public static final StreamCodec<FriendlyByteBuf, CPacketInterfaceScaling> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CPacketInterfaceScaling::scale,
            ByteBufCodecs.BOOL, CPacketInterfaceScaling::divide,
            CPacketInterfaceScaling::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(EAEPActionItems action) {
        int scale = 0;
        boolean divide = false;
        switch (action) {
            case MUL2 -> scale = 2;
            case MUL3 -> scale = 3;
            case MUL5 -> scale = 5;
            case DIV2 -> {
                scale = 2;
                divide = true;
            }
            case DIV3 -> {
                scale = 3;
                divide = true;
            }
            case DIV5 -> {
                scale = 5;
                divide = true;
            }
        }
        if (scale > 0)
            PacketDistributor.sendToServer(new CPacketInterfaceScaling(scale, divide));
    }

    public static void handle(final CPacketInterfaceScaling packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // 支持 AE2 原版接口和 ExtendedAE 扩展接口（若存在）
            InterfaceMenu menu = null;
            ContainerExInterface exMenu = null;
            if (player.containerMenu instanceof InterfaceMenu im) {
                menu = im;
            } else if (player.containerMenu instanceof ContainerExInterface cem) {
                exMenu = cem;
            } else {
                return;
            }

            try {
                var logic = (menu != null ? menu.getHost() : exMenu.getHost()).getInterfaceLogic();
                var config = logic.getConfig();
                // 对所有配置槽统一生效
                int size = config.size();
                for (int index = 0; index < size; index++) {
                    var stack = config.getStack(index);
                    if (stack == null) continue;

                    long amount = stack.amount();
                    int scale = packet.scale;

                    long scaledAmount = 0;
                    if (packet.divide) {
                        if (amount % scale > 0) continue;
                        scaledAmount = amount / scale;
                    } else scaledAmount = amount * scale;

                    config.setStack(index, new GenericStack(stack.what(), scaledAmount));
                }
            } catch (Throwable ignored) {
            }
        });
    }
}
