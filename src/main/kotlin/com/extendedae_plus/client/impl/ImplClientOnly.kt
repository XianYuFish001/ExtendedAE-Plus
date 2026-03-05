package com.extendedae_plus.client.impl

import com.extendedae_plus.util.UtilClient
import net.minecraft.client.gui.screens.Screen

object ImplClientOnly : UtilClient {
    override fun _shift() = Screen.hasShiftDown()

    override fun _ctrl() = Screen.hasControlDown()

    override fun _alt() = Screen.hasAltDown()
}