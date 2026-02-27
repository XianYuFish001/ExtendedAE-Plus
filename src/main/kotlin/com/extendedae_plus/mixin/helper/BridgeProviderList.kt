package com.extendedae_plus.mixin.helper

import appeng.api.implementations.blockentities.PatternContainerGroup
import appeng.helpers.patternprovider.PatternContainer

interface BridgeProviderList {
    fun `eaep$getProviderList`(): Map<PatternContainerGroup, List<PatternContainer>>
}
