package com.extendedae_plus.network.helper;

import appeng.client.gui.AEBaseScreen;
import appeng.menu.SlotSemantics;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.client.screen.ScreenProviderList;
import com.extendedae_plus.client.screen.WrapperScreenAE;
import com.extendedae_plus.client.screen.labelLink.ScreenLabelLink;
import com.extendedae_plus.mixin.bridge.BridgePlanToEncode;
import com.extendedae_plus.network.SPacketHighlightPatternSlot;
import com.extendedae_plus.network.SPacketLabelList;
import com.extendedae_plus.network.SPacketProvidersInfo;
import com.extendedae_plus.network.SPacketSetProviderPage;
import com.glodblock.github.extendedae.client.gui.GuiExPatternProvider;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
public class ImplHandlerClient implements HelperHandlerClient {
    @Override
    public void encodeFinished(LocalPlayer player) {
        if (!(player.containerMenu instanceof BridgePlanToEncode helper)) return;
        helper.eaep$execute();
    }

    @Override
    public void highlightSlotPattern(SPacketHighlightPatternSlot packet, LocalPlayer player) {
        // TODO Impl
    }

    @Override
    public void labelList(SPacketLabelList packet, LocalPlayer player) {
        if (!(Minecraft.getInstance().screen instanceof ScreenLabelLink screen)) return;
        screen.setLabelsMapped(packet.labels());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void providersInfo(SPacketProvidersInfo packet, LocalPlayer player) {
        AEBaseScreen<? extends PatternEncodingTermMenu> screenCurrent = null;
        var screenAE = switch (Minecraft.getInstance().screen) {
            case BoMScreen screen -> {
                if (!(screen.old instanceof AEBaseScreen<?> screenParent)
                        || !(screenParent.getMenu() instanceof PatternEncodingTermMenu menu)) yield null;
                screenCurrent = new WrapperScreenAE<>(menu, screen);
                yield screenParent;
            }
            case AEBaseScreen<?> screen -> {
                if (!(screen.getMenu() instanceof PatternEncodingTermMenu)) yield null;
                screenCurrent = (AEBaseScreen<? extends PatternEncodingTermMenu>) screen;
                yield screen;
            }
            case null, default -> null;
        };
        if (screenAE == null) return;
        screenAE.switchToScreen(new ScreenProviderList<>(screenCurrent, packet.info()));
    }

    @Override
    public void setProviderPage(SPacketSetProviderPage packet, LocalPlayer player) {
        try {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof GuiExPatternProvider guiExPatternProvider) {
                Field currentPage = screen.getClass().getDeclaredField("eap$currentPage");
                currentPage.setAccessible(true);
                currentPage.setInt(guiExPatternProvider, packet.page());

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
