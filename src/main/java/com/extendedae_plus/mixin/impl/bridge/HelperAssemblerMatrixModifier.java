package com.extendedae_plus.mixin.impl.bridge;

import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter;

public interface HelperAssemblerMatrixModifier {
    void eaep$addCrafter(TileAssemblerMatrixCrafter crafter);

    void eaep$addSpeedCore();

    void eaep$updateCrafter(TileAssemblerMatrixCrafter crafter);
}
