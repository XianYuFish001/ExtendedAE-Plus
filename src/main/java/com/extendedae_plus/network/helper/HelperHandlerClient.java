package com.extendedae_plus.network.helper;

import com.extendedae_plus.network.SPacketHighlightPatternSlot;
import com.extendedae_plus.network.SPacketLabelList;
import com.extendedae_plus.network.SPacketProvidersInfo;
import com.extendedae_plus.network.SPacketSetProviderPage;
import net.minecraft.client.player.LocalPlayer;

import java.lang.reflect.InvocationTargetException;

public interface HelperHandlerClient {
    HelperHandlerClient instance = getInstance();

    private static HelperHandlerClient getInstance() {
        try {
            return (HelperHandlerClient) Class.forName("com.extendedae_plus.network.helper.ImplHandlerClient")
                    .getConstructor()
                    .newInstance();
        } catch (ClassNotFoundException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    void encodeFinished(LocalPlayer player);

    void highlightSlotPattern(SPacketHighlightPatternSlot packet, LocalPlayer player);

    void labelList(SPacketLabelList packet, LocalPlayer player);

    void providersInfo(SPacketProvidersInfo packet, LocalPlayer player);

    void setProviderPage(SPacketSetProviderPage packet, LocalPlayer player);
}
