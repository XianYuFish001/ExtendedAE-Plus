package com.extendedae_plus.common.registry.part.ticker

import appeng.api.config.Actionable
import appeng.api.config.PowerMultiplier
import appeng.api.networking.energy.IEnergyService
import appeng.api.networking.security.IActionSource
import appeng.api.storage.MEStorage
import appeng.api.storage.StorageHelper
import appeng.api.upgrades.IUpgradeInventory
import appeng.core.definitions.AEItems
import com.extendedae_plus.EAEPConfig
import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.integration.helper.ContextModLoaded
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationAppliedFlux
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow

object EnergyExtractor {
    @JvmStatic
    fun calculateMultiplier(upgradeInventory: IUpgradeInventory): Long {
        val result = intArrayOf(1, 1024)
        upgradeInventory.forEach { card ->
            val data = card.get(EAEPDataComponents.CardTicking) ?: return@forEach

            result[0] *= data.multiplier
            result[1] = min(result[1], data.maxMultiplier)
        }
        return min(result[0], result[1]).toLong()
    }

    // 数学不好, 还是ai助我吧
    @JvmStatic
    fun calculateEnergyCost(
        upgradeInventory: IUpgradeInventory,
        speedMultiplier: Long,
        costMultiplier: Double
    ): Double {
        val energyCardCount = upgradeInventory.getInstalledUpgrades(AEItems.ENERGY_CARD)
        val baseCost = EAEPConfig.BaseTickerEnergyCost

        // 当multiplier为1时，能量消耗为baseCost
        // 当multiplier达到1024时，能量消耗达到2147483647
        // cost = baseCost * (growthFactor)^(log2(speedMultiplier))
        val log2Multiplier = ln(speedMultiplier.toDouble()) / ln(2.0)
        val growthFactor = (2147483647.0 / baseCost).pow(1.0 / 10.0)

        val rawCost: Double = baseCost * growthFactor.pow(log2Multiplier)
        return rawCost * calculateEnergyRemainingRatio(energyCardCount) * costMultiplier
    }

    fun calculateEnergyRemainingRatio(cardCount: Int): Double {
        // 边际效应递减
        var energyCardEffect = 1.0
        if (cardCount > 0) {
            // effect = 0.9 * (0.5/0.9)^((n-1)/7)
            energyCardEffect = 0.9 * (0.5 / 0.9).pow((cardCount - 1) / 7.0)
        }
        return energyCardEffect
    }

    @JvmStatic
    fun calculateAndExtractEnergy(host: PartTicker, multiplier: Long, externalCost: Double) =
        extractEnergy(
            host, calculateEnergyCost(
                host.upgrades,
                multiplier,
                externalCost
            )
        )

    fun extractEnergy(host: PartTicker, energyCost: Double): Boolean {
        val grid = host.mainNode.grid ?: return false

        val energyService = grid.energyService
        val storage = grid.storageService.inventory

        val source = IActionSource.ofMachine(host)

        if (EAEPConfig.AllowDiskEnergy
            && ContextModLoaded.AppliedFlux()
            && tryExtractFE(source, energyService, storage, energyCost)
        ) {
            return true
        } else {
            val simulated = energyService.extractAEPower(
                energyCost,
                Actionable.SIMULATE,
                PowerMultiplier.CONFIG
            )
            if (simulated < energyCost) return false

            val extracted = energyService.extractAEPower(
                energyCost,
                Actionable.MODULATE,
                PowerMultiplier.CONFIG
            )
            return extracted >= energyCost
        }
    }

    private fun tryExtractFE(
        source: IActionSource,
        energyService: IEnergyService,
        storage: MEStorage,
        requiredPower: Double
    ): Boolean {
        val feKey = ManagerIntegration<IntegrationAppliedFlux>()
            ?.keyFlux
            ?: return false

        val amount = requiredPower.toLong() shl 1

        // 模拟提取 FE
        var feExtracted = StorageHelper.poweredExtraction(
            energyService,
            storage,
            feKey,
            amount,
            source,
            Actionable.SIMULATE
        )

        // 执行实际提取
        if (feExtracted >= amount) feExtracted = StorageHelper.poweredExtraction(
            energyService,
            storage,
            feKey,
            amount,
            source,
            Actionable.MODULATE
        )

        return feExtracted >= amount
    }
}
