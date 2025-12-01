package com.extendedae_plus.mixin.impl;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderTarget;
import com.extendedae_plus.common.impl.pattern.smartDoubling.SmartDoublingAwarePattern;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.settings.StateSmartBlocking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ProviderSettingsImplementations {
    private static final List<IConfigManager> flagChanging = new ArrayList<>();

    public static void onSettingsChanged(IConfigManager manager,
                                         Setting<?> setting,
                                         Runnable doublingUpdater) {
        if (flagChanging.contains(manager)) return;

        if (Settings.BLOCKING_MODE.equals(setting)) {
            var stateBlocking = switch (manager.getSetting(Settings.BLOCKING_MODE)) {
                case NO -> StateSmartBlocking.DISABLED_BY_SUPER;
                case YES -> StateSmartBlocking.DISABLED;
                default -> null;
            };
            if (stateBlocking != null) {
                flagChanging.add(manager);
                manager.putSetting(ModSettings.SMART_BLOCKING, stateBlocking);
                flagChanging.remove(manager);
            }
        } else if (ModSettings.SMART_BLOCKING.equals(setting)
                && StateSmartBlocking.ENABLED.equals(manager.getSetting(setting))) {
            flagChanging.add(manager);
            manager.putSetting(Settings.BLOCKING_MODE, YesNo.YES);
            flagChanging.remove(manager);
        } else if (ModSettings.SMART_DOUBLING.equals(setting)) {
            doublingUpdater.run();
        }
    }

    public static boolean checkCanBlock(boolean isBlocking,
                                        IConfigManager configManager,
                                        PatternProviderTarget target,
                                        Set<AEKey> inputs,
                                        IPatternDetails patternDetails) {
        return target.containsPatternInput(inputs)
                && (!isBlocking
                || (!StateSmartBlocking.ENABLED.equals(configManager.getSetting(ModSettings.SMART_BLOCKING))
                || !matchBlockingInputs(target, patternDetails)));
    }

    public static void updateDoublingState(IConfigManager configManager, List<IPatternDetails> patterns) {
        patterns.forEach(pattern -> {
            if (!(pattern instanceof SmartDoublingAwarePattern doublingPattern)) return;
            doublingPattern.eap$setAllowScaling(
                    YesNo.YES.equals(configManager.getSetting(ModSettings.SMART_DOUBLING)));
        });
    }

    private static boolean matchBlockingInputs(PatternProviderTarget target, IPatternDetails patternDetails) {
        for (var slot : patternDetails.getInputs()) {
            var inputs = Arrays.stream(slot.getPossibleInputs())
                    .map(input -> input.what().dropSecondary())
                    .collect(Collectors.toSet());
            if (!target.containsPatternInput(inputs)) return false;
        }
        return true;
    }
}
