package com.extendedae_plus.network;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.common.impl.pattern.PatternProviderData;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorProviderMenu;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

/**
 * C2S：请求对当前打开的样板供应器执行样板数量缩放（倍增或除法）。
 */
@EAEPNetworkPacket("scale_patterns")
public record CPacketScalePatterns(int scale, boolean mul) implements CPacketGeneric {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final StreamCodec<FriendlyByteBuf, CPacketScalePatterns> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CPacketScalePatterns::scale,
            ByteBufCodecs.BOOL, CPacketScalePatterns::mul,
            CPacketScalePatterns::new
    );

    public static void send(EAEPActionItems action) {
        int scale = 0;
        boolean mul = false;

        switch (action) {
            case MUL2 -> {
                scale = 2;
                mul = true;
            }
            case MUL3 -> {
                scale = 3;
                mul = true;
            }
            case MUL5 -> {
                scale = 5;
                mul = true;
            }
            case DIV2 -> scale = 2;
            case DIV3 -> scale = 3;
            case DIV5 -> scale = 5;
        }

        if (scale > 0)
            PacketDistributor.sendToServer(new CPacketScalePatterns(scale, mul));
    }

    @Override
    public void handleServer(ServerPlayer player) {
        if (!(player.containerMenu instanceof PatternProviderMenu menu)) return;

        try {
            var accessor = (AccessorProviderMenu) menu;
            PatternProviderLogic logic = accessor.eaep$getProviderLogic();
            if (logic == null) return;

            double scale = this.scale;
            boolean multiply = this.mul;

            PatternProviderData.PatternScalingResult result;
            if (multiply) {
                result = PatternProviderData.multiplyPatternAmounts(logic, scale);
            } else {
                result = PatternProviderData.dividePatternAmounts(logic, scale);
            }

            logic.saveChanges();

            // 回显结果到玩家
            player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                            .addStr("pattern_scaling")
                            .addStr(multiply, "mul", "div")
                            .args(
                                    scale,
                                    result.getTotalPatterns(),
                                    result.getScaledPatterns(),
                                    result.getFailedPatterns()
                            )
                            .build(),
                    false
            );

        } catch (Throwable t) {
            LOGGER.error("[EAEP] Handle ScalePatternsC2SPacket failed", t);
        }
    }
}
