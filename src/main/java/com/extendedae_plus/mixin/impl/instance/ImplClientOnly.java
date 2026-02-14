package com.extendedae_plus.mixin.impl.instance;

import com.extendedae_plus.mixin.impl.HelperClientOnly;
import net.minecraft.client.gui.screens.Screen;

public class ImplClientOnly implements HelperClientOnly {
    @Override
    public boolean hasControlDown() {
        return Screen.hasControlDown();
    }

    @Override
    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }
}
