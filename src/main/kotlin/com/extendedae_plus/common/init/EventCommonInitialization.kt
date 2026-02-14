package com.extendedae_plus.common.init

import appeng.api.parts.PartModels
import appeng.api.storage.StorageCells
import appeng.blockentity.crafting.CraftingBlockEntity
import appeng.items.parts.PartModelsHelper
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.impl.menuLocator.CuriosItemLocator
import com.extendedae_plus.common.registry.block.EAEPCraftingUnitType
import com.extendedae_plus.common.registry.item.infinityBigIntegerCell.InfinityBigIntegerCellHandler
import com.extendedae_plus.common.registry.part.ticker.PartTicker
import com.extendedae_plus.util.UtilCodec
import com.mojang.logging.LogUtils
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
object EventCommonInitialization {
    private val LOGGER = LogUtils.getLogger()

    @SubscribeEvent
    fun onCapabilitiesRegistering(event: RegisterCapabilitiesEvent) {
        ModBlockEntities.registerBlockEntityCapability(event)
    }

    @SubscribeEvent
    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        partModel()

        event.enqueueWork {
            ModUpgradeCards.init()
            cellHandler()
            locator()
            blockEntity()
        }.whenComplete { _: Void, exception: Throwable? ->
            if (exception != null) LOGGER.error("Common Initialize failed ", exception)
        }
    }

    private fun cellHandler() {
        StorageCells.addCellHandler(InfinityBigIntegerCellHandler.INSTANCE)
    }

    private fun locator() {
        MenuLocators.register(
            CuriosItemLocator::class.java,
            UtilCodec.encodeReversed(CuriosItemLocator.STREAM_CODEC),
            CuriosItemLocator.STREAM_CODEC::decode
        )
    }

    private fun partModel() {
        PartModels.registerModels(PartModelsHelper.createModels(PartTicker::class.java))
    }

    private fun blockEntity() {
        ModBlockEntities.bindAEBlockEntity()

        EAEPCraftingUnitType.entries.forEach { unit: EAEPCraftingUnitType ->
            unit.block.get().setBlockEntity(
                CraftingBlockEntity::class.java,
                ModBlockEntities.EAEP_CRAFTING_UNIT.get(),
                null, null
            )
        }
    }
}
