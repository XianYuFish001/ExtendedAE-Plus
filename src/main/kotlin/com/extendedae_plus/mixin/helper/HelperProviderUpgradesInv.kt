package com.extendedae_plus.mixin.helper

import appeng.api.upgrades.IUpgradeInventory

interface HelperProviderUpgradesInv {
    fun `eaep$getUpgradeInventory`(): IUpgradeInventory

    fun `eaep$addAction`(action: Runnable)
}
