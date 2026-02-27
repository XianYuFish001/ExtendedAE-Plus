package com.extendedae_plus.mixin.helper

import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter

interface HelperAssemblerMatrixModifier {
    fun `eaep$addCrafter`(crafter: TileAssemblerMatrixCrafter)

    fun `eaep$addSpeedCore`()

    fun `eaep$updateCrafter`(crafter: TileAssemblerMatrixCrafter)

    fun `eaep$markUploadCore`()

    fun `eaep$hasUploadCore`(): Boolean
}
