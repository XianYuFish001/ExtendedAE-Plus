package com.extendedae_plus.mixin.helper

import com.extendedae_plus.util.UtilTextComponent.ComponentColorful

interface HelperComponentColorful {
    fun `eaep$getColored`(): ComponentColorful?

    fun `eaep$clearColored`()
}
