package com.extendedae_plus.mixin.impl.bridge;

import com.extendedae_plus.util.UtilTextComponent;
import org.jetbrains.annotations.Nullable;

public interface HelperComponentColorful {
    @Nullable
    UtilTextComponent.ComponentColorful eaep$getColored();

    void eaep$clearColored();
}
