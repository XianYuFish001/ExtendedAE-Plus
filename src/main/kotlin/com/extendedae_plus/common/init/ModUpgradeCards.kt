package com.extendedae_plus.common.init

import appeng.api.upgrades.Upgrades
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import appeng.core.localization.GuiText
import com.extendedae_plus.integration.ContextModLoaded
import com.extendedae_plus.util.extension.addAll
import com.glodblock.github.extendedae.common.EAESingletons
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.registries.DeferredItem
import net.pedroksl.advanced_ae.common.definitions.AAEBlocks
import net.pedroksl.advanced_ae.common.definitions.AAEItems

object ModUpgradeCards {
    /** 在commonSetup时注册 */
    fun init() {
        Upgrades.add(AEItems.ENERGY_CARD, ModItems.Ticker.get(), 8)
        Upgrades.add(ModItems.CardTicking.get(), ModItems.Ticker.get(), 4)

        val interfaceGroup = GuiText.Interface.translationKey
        val patternProviderGroup = "group.pattern_provider.name"
        val ioBusGroup = GuiText.IOBuses.translationKey
        val storageGroup = "group.storage.name"

        val machineGroups = listOf(
            mutableListOf(
                AEBlocks.INTERFACE,
                AEParts.INTERFACE,
                EAESingletons.EX_INTERFACE,
                EAESingletons.EX_INTERFACE_PART,
                EAESingletons.OVERSIZE_INTERFACE,
                EAESingletons.OVERSIZE_INTERFACE_PART
            ),
            mutableListOf(
                AEBlocks.PATTERN_PROVIDER,
                AEParts.PATTERN_PROVIDER,
                EAESingletons.EX_PATTERN_PROVIDER,
                EAESingletons.EX_PATTERN_PROVIDER_PART
            ),
            mutableListOf(
                AEParts.IMPORT_BUS,
                AEParts.EXPORT_BUS,
                EAESingletons.EX_IMPORT_BUS,
                EAESingletons.EX_EXPORT_BUS,
                EAESingletons.TAG_EXPORT_BUS,
                EAESingletons.MOD_EXPORT_BUS,
                EAESingletons.PRECISE_EXPORT_BUS,
                EAESingletons.THRESHOLD_EXPORT_BUS

            ),
            mutableListOf(
                AEParts.STORAGE_BUS,
                EAESingletons.TAG_STORAGE_BUS,
                EAESingletons.MOD_STORAGE_BUS,
                EAESingletons.PRECISE_STORAGE_BUS
            )
        )
        if (ContextModLoaded.AdvancedAE.isLoaded) {
            machineGroups[1].addAll(
                    AAEBlocks.SMALL_ADV_PATTERN_PROVIDER,
                    AAEBlocks.ADV_PATTERN_PROVIDER,
                    AAEItems.SMALL_ADV_PATTERN_PROVIDER,
                    AAEItems.ADV_PATTERN_PROVIDER
            )

            machineGroups[2].add(AAEItems.IMPORT_EXPORT_BUS)
            machineGroups[2].add(AAEItems.STOCK_EXPORT_BUS)
        }

        this.register(ModItems.CardChannel, machineGroups[0], 1, interfaceGroup)
        this.register(ModItems.CardChannel, machineGroups[1], 1, patternProviderGroup)
        this.register(ModItems.CardChannel, machineGroups[2], 1, ioBusGroup)
        this.register(ModItems.CardChannel, machineGroups[3], 1, storageGroup)

        this.register(ModItems.CardAutoCompletion, machineGroups[1], 1, patternProviderGroup)
    }

    private fun register(
        card: DeferredItem<*>,
        machines: MutableCollection<ItemLike>,
        maxSupported: Int,
        tooltipGroup: String
    ) = machines.forEach { machine ->
        Upgrades.add(
            card.get(),
            machine,
            maxSupported,
            tooltipGroup
        )
    }
}