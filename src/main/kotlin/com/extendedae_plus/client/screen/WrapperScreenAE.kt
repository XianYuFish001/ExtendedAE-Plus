package com.extendedae_plus.client.screen

import appeng.client.gui.AEBaseScreen
import appeng.client.gui.style.StyleManager
import appeng.menu.AEBaseMenu
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class WrapperScreenAE<TMenu : AEBaseMenu>(menu: TMenu, private val toWrap: Screen) : AEBaseScreen<TMenu>(
    menu,
    menu.playerInventory,
    Component.empty(),
    StyleManager.loadStyleDoc("/screens/common/common.json")
) {
    override fun init() {
        Minecraft.getInstance().screen = null
        Minecraft.getInstance().setScreen(this.toWrap)
    }
}
