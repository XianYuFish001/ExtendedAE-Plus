package com.extendedae_plus.network;

import appeng.menu.SlotSemantics;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import com.extendedae_plus.network.base.SPacketGeneric;
import com.glodblock.github.extendedae.client.gui.GuiExPatternProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Field;

/**
 * S2C: 指示客户端在已打开的样板供应器界面切换到指定页
 */
@EAEPNetworkPacket
public record SPacketSetProviderPage(int page) implements SPacketGeneric {
    public static final Type<SPacketSetProviderPage> TYPE = PacketGeneric.createType("set_provider_page");

    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSetProviderPage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SPacketSetProviderPage::page,
            SPacketSetProviderPage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handleClient(LocalPlayer player) {
        try {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof GuiExPatternProvider guiExPatternProvider) {
                Field currentPage = screen.getClass().getDeclaredField("eap$currentPage");
                currentPage.setAccessible(true);
                currentPage.setInt(guiExPatternProvider, this.page);

                guiExPatternProvider.repositionSlots(SlotSemantics.ENCODED_PATTERN);
                guiExPatternProvider.repositionSlots(SlotSemantics.STORAGE);

                Field hs = screen.getClass().getDeclaredField("hoveredSlot");
                hs.setAccessible(true);
                hs.set(screen, null);
            }
        } catch (Throwable ignored) {
        }
    }
}


