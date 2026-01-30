package com.extendedae_plus.mixin.bridge;

import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter;

public interface HelperAssemblerMatrixModifier {
    void eaep$addCrafter(TileAssemblerMatrixCrafter crafter);

    void eaep$addSpeedCore();

    void eaep$updateCrafter(TileAssemblerMatrixCrafter crafter);

    void eaep$markUploadCore();

    boolean eaep$hasUploadCore();
}
