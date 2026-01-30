package com.extendedae_plus.mixin.bridge;

import appeng.api.inventories.InternalInventory;
import appeng.helpers.patternprovider.PatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;

public interface HelperProviderMenu {
    default PatternProviderLogic getProviderLogicVanilla() {
        return null;
    }

    default AdvPatternProviderLogic getProviderLogicAdv() {
        return null;
    }

    default InternalInventory getInvPattern() {
        var logicVanilla = this.getProviderLogicVanilla();
        var logicAdv = this.getProviderLogicAdv();
        if (logicVanilla != null) return logicVanilla.getPatternInv();
        else return logicAdv.getPatternInv();
    }
}
