package com.extendedae_plus.mixin.impl.bridge;

import appeng.api.upgrades.IUpgradeInventory;

public interface HelperProviderUpgradesInv {
    IUpgradeInventory eaep$getUpgradeInventory();

    void eaep$addAction(Runnable action);
}
