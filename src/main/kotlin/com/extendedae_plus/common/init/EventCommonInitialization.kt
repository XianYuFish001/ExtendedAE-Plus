package com.extendedae_plus.common.init

import appeng.api.parts.PartModels
import appeng.api.storage.StorageCells
import appeng.blockentity.crafting.CraftingBlockEntity
import appeng.items.parts.PartModelsHelper
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.impl.menuLocator.CuriosItemLocator
import com.extendedae_plus.common.registry.block.EAEPCraftingUnit
import com.extendedae_plus.common.registry.item.infinityBigIntegerCell.InfinityBigIntegerCellHandler
import com.extendedae_plus.common.registry.part.ticker.PartTicker
import com.fish.fishlib.util.extension.invoke
import com.fish.fishlib.util.extension.invokeReversed
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import org.slf4j.LoggerFactory

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
object EventCommonInitialization {
    private val Logger = LoggerFactory.getLogger("EAEP/Init/Common")

    @SubscribeEvent
    fun onCapabilitiesRegistering(event: RegisterCapabilitiesEvent) {
        EAEPTiles.regTileCapability(event)
    }

    @SubscribeEvent
    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        this.partModel()

        event.enqueueWork {
            EAEPUpgradeCards.init()
            this.cellHandler()
            this.locator()
            this.tile()
        }.whenComplete { _, exception ->
            if (exception != null)
                Logger.error("Failed to initialize", exception)
        }
    }

    private fun cellHandler() {
        StorageCells.addCellHandler(InfinityBigIntegerCellHandler.INSTANCE)
    }

    private fun locator() {
        MenuLocators.register(
            CuriosItemLocator::class.java,
            CuriosItemLocator.streamCodec::encode.invokeReversed(),
            CuriosItemLocator.streamCodec::decode
        )
    }

    private fun partModel() {
        PartModels.registerModels(PartModelsHelper.createModels(PartTicker::class.java))
    }

    private fun tile() {
        EAEPTiles.bindTileAE()

        EAEPCraftingUnit.entries.forEach {
            it.block().setBlockEntity(
                CraftingBlockEntity::class.java,
                EAEPTiles.UnitCraftingUniversal.get(),
                null, null
            )
        }
    }
}
