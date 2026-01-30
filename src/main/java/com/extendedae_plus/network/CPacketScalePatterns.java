package com.extendedae_plus.network;

import appeng.api.crafting.PatternDetailsHelper;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.mixin.bridge.HelperProviderMenu;
import com.extendedae_plus.mixin.extension.IScaledPattern;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.util.extension.ExtensionScaledPattern;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * C2S：请求对当前打开的样板供应器执行样板数量缩放（倍增或除法）。
 */
@EAEPNetworkPacket("scale_patterns")
@ExtensionMethod(ExtensionScaledPattern.class)
public record CPacketScalePatterns(int scale) implements CPacketGeneric {
    public static final StreamCodec<FriendlyByteBuf, CPacketScalePatterns> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CPacketScalePatterns::scale,
            CPacketScalePatterns::new
    );

    public static void send(EAEPActionItems action) {
        var scale = switch (action) {
            case mul2 -> 2;
            case mul3 -> 3;
            case mul5 -> 5;
            case div2 -> -2;
            case div3 -> -3;
            case div5 -> -5;
            default -> 0;
        };

        if (scale == 0) return;
        PacketDistributor.sendToServer(new CPacketScalePatterns(scale));
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof HelperProviderMenu helper)) return;
        var invPattern = helper.getInvPattern();

        invPattern.forEach(stack -> {
            if (!(PatternDetailsHelper.decodePattern(stack, player.serverLevel())
                    instanceof IScaledPattern extension)) return;
            extension.create(this.scale, false).writeToStack(stack);
        });
    }
}
