package com.extendedae_plus.network;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.mixin.core.advancedae.accessor.AccessorAdvProviderMenu;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorProviderMenu;
import com.extendedae_plus.mixin.impl.bridge.ISmartBlockingObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;

import java.util.function.BiConsumer;

/**
 * C2S：切换高级阻挡模式。
 * 不含额外负载，直接基于玩家当前打开的 PatternProviderMenu 进行切换。
 */
public class CPacketToggleSmartBlocking implements CustomPacketPayload {
    public static final Type<CPacketToggleSmartBlocking> TYPE = new Type<>(
            ExtendedAEPlus.getLocation("toggle_smart_blocking"));

    public static final CPacketToggleSmartBlocking INSTANCE = new CPacketToggleSmartBlocking();

    public static final StreamCodec<FriendlyByteBuf, CPacketToggleSmartBlocking> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final CPacketToggleSmartBlocking packet, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            toggleSettings(player.containerMenu, (helper, configManager) -> {
                var stateTo = !helper.eaep$getBlockingState();
                helper.eaep$setBlockingState(stateTo);
                if (stateTo) configManager.putSetting(Settings.BLOCKING_MODE, YesNo.YES);
            });
        });
    }
    
    public static void toggleSettings(AbstractContainerMenu menu, BiConsumer<ISmartBlockingObject, IConfigManager> task) {
        if (menu instanceof PatternProviderMenu) {
            var accessor = (AccessorProviderMenu) menu;
            var logic = accessor.eaep$getProviderLogic();
            if (!(logic instanceof ISmartBlockingObject helper)) return;
            task.accept(helper, logic.getConfigManager());
            logic.saveChanges();
        } else if (menu instanceof AdvPatternProviderMenu) {
            var accessor = (AccessorAdvProviderMenu) menu;
            var logic = accessor.eaep$getProviderLogic();
            if (!(logic instanceof ISmartBlockingObject helper)) return;
            task.accept(helper, logic.getConfigManager());
            logic.saveChanges();
        }
    }

}
