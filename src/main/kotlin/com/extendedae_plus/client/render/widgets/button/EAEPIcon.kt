package com.extendedae_plus.client.render.widgets.button

import appeng.client.gui.Icon
import appeng.client.gui.style.Blitter
import com.extendedae_plus.ExtendedAEPlus
import org.slf4j.LoggerFactory

enum class EAEPIcon(val x: Int, val y: Int, val width: Int = 16, val height: Int = 16) : IButtonIcon {
    Mul2(0, 0),
    Div2(16, 0),
    Mul3(32, 0),
    Div3(48, 0),
    Disconnected(64, 0),
    Connected(80, 0),
    SignalSend(96, 0),
    SignalReceive(112, 0),

    Mul5(0, 16),
    Div5(16, 16),
    PatternSingle(32, 16),
    PatternMulti(48, 16),
    MergeAdjacency(64, 16),
    MergeNone(80, 16),

    BlockingTransparent(0, 32),
    SaveCenter(16, 32),
    SaveUp(32, 32),
    SaveDown(48, 32),

    ListWithChildren(0, 48),
    ListMulti(16, 48),
    CharF(32, 48),
    CharL(48, 48),
    ;

    override val blitter: Blitter by lazy {
        Blitter.texture(
            Texture,
            WIDTH,
            HEIGHT
        ).src(x, y, width, height)
    }

    override val aeIcon: Icon
        get() {
            Logger.warn("Unsupported AEIcon call")
            return Icon.TOOLBAR_BUTTON_BACKGROUND
        }

    private data class AEIcon(override val aeIcon: Icon) : IButtonIcon {
        override val blitter: Blitter
            get() = this.aeIcon.blitter
    }

    companion object {
        private val Logger = LoggerFactory.getLogger("EAEP/Icon")

        val Texture = ExtendedAEPlus.getLocation("textures/gui/icons.png")
        const val WIDTH = 128
        const val HEIGHT = 128

        @JvmStatic
        fun fromAEIcon(aeIcon: Icon): IButtonIcon = AEIcon(aeIcon)
    }
}
