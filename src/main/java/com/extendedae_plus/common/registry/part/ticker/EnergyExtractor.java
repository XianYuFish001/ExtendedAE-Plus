package com.extendedae_plus.common.registry.part.ticker;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.definitions.AEItems;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.integration.ContextModLoaded;

public class EnergyExtractor {
    public static long calculateMultiplier(IUpgradeInventory upgradeInventory) {
        var result = new int[] {1, 1024};
        upgradeInventory.forEach(card -> {
            if (!card.has(ModDataComponents.DATA_TICKING_CARD)) return;
            var data = card.get(ModDataComponents.DATA_TICKING_CARD);
            result[0] *= data.multiplier();
            result[1] = Math.min(result[1], data.maxMultiplier());
        });
        return Math.min(result[0], result[1]);
    }

    // 数学不好, 还是ai助我吧
    public static double calculateEnergyCost(IUpgradeInventory upgradeInventory, long speedMultiplier, double costMultiplier) {
        var energyCardCount = upgradeInventory.getInstalledUpgrades(AEItems.ENERGY_CARD);
        var baseCost = EAEPConfig.TICKER_BASE_COST.getAsInt();

        // 当multiplier为1时，能量消耗为baseCost
        // 当multiplier达到1024时，能量消耗达到2147483647
        // cost = baseCost * (growthFactor)^(log2(speedMultiplier))
        double log2Multiplier = Math.log(speedMultiplier) / Math.log(2.0);
        double growthFactor = Math.pow(2147483647.0 / baseCost, 1.0 / 10.0);

        double rawCost = baseCost * Math.pow(growthFactor, log2Multiplier);
        return rawCost * calculateEnergyRemainingRatio(energyCardCount) * costMultiplier;
    }

    public static double calculateEnergyRemainingRatio(int cardCount) {
        // 边际效应递减
        double energyCardEffect = 1.0;
        if (cardCount > 0) {
            // effect = 0.9 * (0.5/0.9)^((n-1)/7)
            energyCardEffect = 0.9 * Math.pow(0.5 / 0.9, (cardCount - 1) / 7.0);
        }
        return energyCardEffect;
    }

    static boolean calculateAndExtractEnergy(PartTicker host, long multiplier, double externalCost) {
        return extractEnergy(host, calculateEnergyCost(host.getUpgrades(), multiplier, externalCost));
    }

    static boolean extractEnergy(PartTicker host, double energyCost) {
        if (host.getMainNode().getGrid() == null) return false;
        IEnergyService energyService = host.getMainNode().getGrid().getEnergyService();

        MEStorage storage = host.getMainNode().getGrid().getStorageService().getInventory();
        IActionSource source = IActionSource.ofMachine(host);
        if (EAEPConfig.ALLOW_DISK_ENERGY.getAsBoolean()
                && ContextModLoaded.appliedFlux.isLoaded()
                && tryExtractFE(source, energyService, storage, energyCost)) {
            return true;
        } else {
            double simulated = energyService.extractAEPower(energyCost, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            if (simulated < energyCost) return false;

            double extracted = energyService.extractAEPower(energyCost, Actionable.MODULATE, PowerMultiplier.CONFIG);
            return extracted >= energyCost;
        }
    }

    private static boolean tryExtractFE(IActionSource source,
                                        IEnergyService energyService,
                                        MEStorage storage,
                                        double requiredPower) {
        try {
            var clazzFluxKey = Class.forName("com.glodblock.github.appflux.common.me.key.FluxKey");
            var clazzEnergyType = Class.forName("com.glodblock.github.appflux.common.me.key.type.EnergyType");
            AEKey feKey = (AEKey) clazzFluxKey.getMethod("of", clazzEnergyType)
                    .invoke(null, clazzEnergyType.getField("FE").get(null));

            // 模拟提取 FE
            long feExtracted = StorageHelper.poweredExtraction(energyService, storage, feKey,
                    (long) requiredPower << 1, source, Actionable.SIMULATE);

            // 执行实际提取
            if (feExtracted >= (long) requiredPower << 1)
                feExtracted = StorageHelper.poweredExtraction(energyService, storage, feKey,
                        (long) requiredPower << 1, source, Actionable.MODULATE);

            if (feExtracted >= (long) requiredPower << 1) return true;
        } catch (Throwable ignore) {
        }
        return false;
    }
}
