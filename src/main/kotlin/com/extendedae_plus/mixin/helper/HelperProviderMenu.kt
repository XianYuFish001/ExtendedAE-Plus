package com.extendedae_plus.mixin.helper

import appeng.api.inventories.InternalInventory
import appeng.helpers.patternprovider.PatternProviderLogic
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic

interface HelperProviderMenu {
    val providerLogicVanilla: PatternProviderLogic?
        get() = null

    val providerLogicAdv: AdvPatternProviderLogic?
        get() = null

    val invPattern: InternalInventory?
        get() {
            val logicVanilla = this.providerLogicVanilla
            val logicAdv = this.providerLogicAdv
            return logicVanilla?.patternInv ?: logicAdv?.patternInv
        }
}
