package com.extendedae_plus.mixin.bridge;

import appeng.api.upgrades.IUpgradeInventory;

public interface HelperProviderUpgradesInv {
    IUpgradeInventory eaep$getUpgradeInventory();

    void eaep$addAction(Runnable action);
}
