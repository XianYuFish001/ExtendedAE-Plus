package com.extendedae_plus.mixin.impl

import appeng.api.config.Setting
import appeng.api.config.Settings
import appeng.api.config.YesNo
import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEKey
import appeng.api.util.IConfigManager
import appeng.helpers.patternprovider.PatternProviderTarget
import com.extendedae_plus.common.init.EAEPSettings
import com.extendedae_plus.common.registry.settings.StateSmartBlocking
import com.extendedae_plus.util.extension.ExtensionScaledPattern

object ProviderSettingsImplementations {
    @JvmField
    val flagChanging = ArrayList<IConfigManager>()

    @JvmStatic
    fun onSettingsChanged(
        manager: IConfigManager,
        setting: Setting<*>,
        doublingUpdater: Runnable
    ) {
        if (flagChanging.contains(manager)) return

        if (Settings.BLOCKING_MODE == setting) {
            val stateBlocking = when (manager.getSetting(Settings.BLOCKING_MODE)) {
                YesNo.NO -> StateSmartBlocking.DISABLED_BY_SUPER
                YesNo.YES -> StateSmartBlocking.DISABLED
                else -> null
            }
            if (stateBlocking != null) {
                flagChanging.add(manager)
                manager.putSetting(EAEPSettings.smartBlocking, stateBlocking)
                flagChanging.remove(manager)
            }
        } else if (EAEPSettings.smartBlocking == setting
            && StateSmartBlocking.ENABLED == manager.getSetting(setting)
        ) {
            flagChanging.add(manager)
            manager.putSetting(Settings.BLOCKING_MODE, YesNo.YES)
            flagChanging.remove(manager)
        } else if (EAEPSettings.smartDoubling == setting) {
            doublingUpdater.run()
        }
    }

    @JvmStatic
    fun checkCanBlock(
        isBlocking: Boolean,
        configManager: IConfigManager,
        target: PatternProviderTarget,
        inputs: Set<AEKey>,
        patternDetails: IPatternDetails
    ) = target.containsPatternInput(inputs)
            && (!isBlocking
            || (StateSmartBlocking.ENABLED != configManager.getSetting(EAEPSettings.smartBlocking)
            || !matchBlockingInputs(target, patternDetails)))

    @JvmStatic
    fun updateDoublingState(configManager: IConfigManager, patterns: List<IPatternDetails>) =
        patterns.forEach(ExtensionScaledPattern.setState(configManager.getSetting(EAEPSettings.smartDoubling)))

    private fun matchBlockingInputs(target: PatternProviderTarget, patternDetails: IPatternDetails): Boolean {
        for (slot in patternDetails.inputs) {
            val inputs = slot.possibleInputs
                .map { it.what().dropSecondary() }
                .toSet()
            if (!target.containsPatternInput(inputs)) return false
        }
        return true
    }
}
