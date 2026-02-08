package com.extendedae_plus.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;
import appeng.menu.AEBaseMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WrapperScreenAE<TMenu extends AEBaseMenu> extends AEBaseScreen<TMenu> {
    private final Screen toWrap;

    public WrapperScreenAE(TMenu menu, Screen toWrap) {
        super(menu, menu.getPlayerInventory(), Component.empty(), StyleManager.loadStyleDoc("/screens/common/common.json"));
        this.toWrap = toWrap;
    }

    @Override
    protected void init() {
        Minecraft.getInstance().screen = null;
        Minecraft.getInstance().setScreen(this.toWrap);
    }
}
