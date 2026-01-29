package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum EAEPIcon implements IButtonIcon {
    mul2(0, 0),
    div2(16, 0),
    mul3(32, 0),
    div3(48, 0),
    disconnected(64, 0),
    connected(80, 0),
    signalSend(96, 0),
    signalReceive(112, 0),

    mul5(0, 16),
    div5(16, 16),
    patternSingle(32, 16),
    patternMulti(48, 16),
    mergeAdjacency(64, 16),
    mergeNone(80, 16),

    blockingTransparent(0, 32),
    saveCenter(16, 32),
    saveUp(32, 32),
    saveDown(48, 32),

    listWithChildren(0, 48),
    listMulti(16, 48),
    charF(32, 48),
    charL(48, 48),

    ;

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    private static final Logger LOGGER = LoggerFactory.getLogger("[EAEP/Icon]");

    public static final ResourceLocation TEXTURE =
            ExtendedAEPlus.getLocation("textures/gui/icons.png");
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    EAEPIcon(int x, int y) {
        this(x, y, 16, 16);
    }

    EAEPIcon(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT)
                .src(x, y, width, height);
    }

    @Override
    public Icon getAEIcon() {
        LOGGER.warn("Unsupported AEIcon call");
        return Icon.TOOLBAR_BUTTON_BACKGROUND;
    }

    public static IButtonIcon fromAEIcon(Icon aeIcon) {
        return new AEIcon(aeIcon);
    }

    private record AEIcon(Icon aeIcon) implements IButtonIcon {
        @Override
        public Blitter getBlitter() {
            return this.aeIcon.getBlitter();
        }

        @Override
        public Icon getAEIcon() {
            return this.aeIcon;
        }
    }
}
