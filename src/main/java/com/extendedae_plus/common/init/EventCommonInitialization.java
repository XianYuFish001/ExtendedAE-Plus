package com.extendedae_plus.common.init;

import appeng.api.parts.IPart;
import appeng.api.parts.PartModels;
import appeng.api.storage.StorageCells;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.impl.menuLocator.CuriosItemLocator;
import com.extendedae_plus.common.registry.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.registry.item.infinityBigIntegerCell.InfinityBigIntegerCellHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

import java.util.Arrays;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class EventCommonInitialization {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onCapabilitiesRegistering(RegisterCapabilitiesEvent event) {
        ModBlockEntities.onCapabilitiesRegistering(event);
    }

    @SubscribeEvent
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        partModel();

        event.enqueueWork(() -> {
            ModUpgradeCards.init();

            cellHandler();
            locator();
            blockEntity();
        }).whenComplete((v, err) -> {
            if (err != null) LOGGER.warn("Common Initialize failed ", err);
        });
    }

    private static void cellHandler() {
        StorageCells.addCellHandler(InfinityBigIntegerCellHandler.INSTANCE);
    }

    private static void locator() {
        MenuLocators.register(
                CuriosItemLocator.class,
                CuriosItemLocator::writeToPacket,
                CuriosItemLocator::readFromPacket
        );
    }

    private static void partModel() {
        PartModels.registerModels(PartModelsHelper.createModels(
                ModItems.PART_TICKER.get().getPartClass().asSubclass(IPart.class)));
    }

    private static void blockEntity() {
        ModBlockEntities.onBlockEntityBinding();

        Arrays.stream(EAEPCraftingUnitType.values()).forEach(unit ->
                unit.getBlock().get().setBlockEntity(
                        CraftingBlockEntity.class,
                        ModBlockEntities.EAEP_CRAFTING_UNIT.get(),
                        null, null));
    }
}
