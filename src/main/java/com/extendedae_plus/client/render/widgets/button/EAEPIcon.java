package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.resources.ResourceLocation;

public enum EAEPIcon implements IButtonIcon {
    MUL2(0, 0),
    DIV2(16, 0),
    MUL3(32, 0),
    DIV3(48, 0),

    MUL5(0, 16),
    DIV5(16, 16),
    PATTERN_SINGLE(32, 16),
    PATTERN_MULTI(48, 16),

    BLOCKING_TRANSPARENT(0, 32),
    SAVE_CENTER(16, 32),
    SAVE_UP(32, 32),
    SAVE_DOWN(48, 32),

    ;

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public static final ResourceLocation TEXTURE =
            ExtendedAEPlus.getLocation("textures/gui/icons.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

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
        return Icon.INVALID;
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
