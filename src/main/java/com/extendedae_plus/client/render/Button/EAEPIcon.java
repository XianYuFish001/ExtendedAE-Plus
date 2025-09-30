package com.extendedae_plus.client.render.Button;

import appeng.client.gui.style.Blitter;
import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.resources.ResourceLocation;

public enum EAEPIcon {
    MULTIPLY5(0, 0),
    DIVIDE5(16, 0),
    MULTIPLY2(32, 0),
    DIVIDE2(48, 0),
    MULTIPLY10(0, 16),
    DIVIDE10(16, 16);

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExtendedAEPlus.MODID, "textures/gui/icons.png");
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

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT)
                .src(x, y, width, height);
    }
}
