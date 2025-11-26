package com.extendedae_plus.network;

import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.mixin.core.advancedae.accessor.AccessorAdvProviderMenu;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorProviderMenu;
import com.extendedae_plus.mixin.impl.bridge.ISmartDoublingObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;

/**
 * C2S：切换智能翻倍启用状态。
 * 不含额外负载，基于玩家当前打开的 PatternProviderMenu 进行切换。
 */
public class ToggleSmartDoublingC2SPacket implements CustomPacketPayload {
    public static final Type<ToggleSmartDoublingC2SPacket> TYPE = new Type<>(
            ExtendedAEPlus.getLocation("toggle_smart_doubling"));

    public static final ToggleSmartDoublingC2SPacket INSTANCE = new ToggleSmartDoublingC2SPacket();

    public static final StreamCodec<FriendlyByteBuf, ToggleSmartDoublingC2SPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    private ToggleSmartDoublingC2SPacket() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ToggleSmartDoublingC2SPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            var containerMenu = player.containerMenu;
            if (containerMenu instanceof PatternProviderMenu menu) {
                var accessor = (AccessorProviderMenu) menu;
                var logic = accessor.eaep$getProviderLogic();
                if (logic instanceof ISmartDoublingObject holder) {
                    boolean current = holder.eaep$getDoublingState();
                    boolean next = !current;
                    holder.eaep$setDoublingState(next);
                    logic.saveChanges();
                }
            } else if (containerMenu instanceof AdvPatternProviderMenu menu){
                var accessor = (AccessorAdvProviderMenu) menu;
                var logic = accessor.eaep$getProviderLogic();
                if (logic instanceof ISmartDoublingObject holder) {
                    boolean current = holder.eaep$getDoublingState();
                    boolean next = !current;
                    holder.eaep$setDoublingState(next);
                    logic.saveChanges();
                }
            }
        });
    }
}
