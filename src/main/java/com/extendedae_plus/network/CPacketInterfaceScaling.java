package com.extendedae_plus.network;

import appeng.api.stacks.GenericStack;
import appeng.menu.implementations.InterfaceMenu;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.glodblock.github.extendedae.container.ContainerExInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * C2S：调整 ME 接口配置槽位(标记物品)的数量。
 * 支持按因子倍增或整除，且保持最小值为 1。
 */
@EAEPNetworkPacket("interface_scaling")
public record CPacketInterfaceScaling(int scale, boolean divide) implements CPacketGeneric {
    public static final StreamCodec<FriendlyByteBuf, CPacketInterfaceScaling> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CPacketInterfaceScaling::scale,
            ByteBufCodecs.BOOL, CPacketInterfaceScaling::divide,
            CPacketInterfaceScaling::new
    );

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

    public void handleServer(final ServerPlayer player) {
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
                int scale = this.scale;

                long scaledAmount = 0;
                if (this.divide) {
                    if (amount % scale > 0) continue;
                    scaledAmount = amount / scale;
                } else scaledAmount = amount * scale;

                config.setStack(index, new GenericStack(stack.what(), scaledAmount));
            }
        } catch (Throwable ignored) {
        }
    }
}
