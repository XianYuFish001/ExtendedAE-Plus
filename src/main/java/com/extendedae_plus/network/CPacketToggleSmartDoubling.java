package com.extendedae_plus.network;

import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.mixin.core.advancedae.accessor.AccessorAdvProviderMenu;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorProviderMenu;
import com.extendedae_plus.mixin.impl.bridge.ISmartDoublingObject;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;

/**
 * C2S：切换智能翻倍启用状态。
 * 不含额外负载，基于玩家当前打开的 PatternProviderMenu 进行切换。
 */
@EAEPNetworkPacket
public class CPacketToggleSmartDoubling implements CPacketGeneric {
    public static final Type<CPacketToggleSmartDoubling> TYPE = PacketGeneric.createType("toggle_smart_doubling");

    public static final CPacketToggleSmartDoubling INSTANCE = new CPacketToggleSmartDoubling();

    public static final StreamCodec<FriendlyByteBuf, CPacketToggleSmartDoubling> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
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
    }
}
