package com.extendedae_plus.network;

import appeng.api.crafting.PatternDetailsHelper;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorProviderMenu;
import com.extendedae_plus.mixin.extension.ExtensionScaledPattern;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * C2S：请求对当前打开的样板供应器执行样板数量缩放（倍增或除法）。
 */
@EAEPNetworkPacket("scale_patterns")
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
        if (!(player.containerMenu instanceof AccessorProviderMenu helper)) return;

        var logic = helper.getProviderLogic();
        var invPattern = logic.getPatternInv();

        invPattern.forEach(stack -> {
            var pattern = PatternDetailsHelper.decodePattern(stack, player.serverLevel());
            if (pattern == null) return;

            ExtensionScaledPattern.of(pattern, true)
                    .map(extension ->
                            extension.eaep$create(this.scale, false))
                    .ifPresent(scaled -> ExtensionScaledPattern.writeToStack(stack, scaled));
        });
    }
}
