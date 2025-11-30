package com.extendedae_plus.common.event;

import appeng.api.AECapabilities;
import appeng.api.parts.IPart;
import appeng.api.parts.PartModels;
import appeng.api.storage.StorageCells;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.block.uploadCore.UploadCoreBlockEntity;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.impl.menuLocator.CuriosItemLocator;
import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.common.init.ModBlocks;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModUpgradeCards;
import com.extendedae_plus.common.item.infinityBigIntegerCell.InfinityBigIntegerCellHandler;
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
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.WIRELESS_TRANSCEIVER.get(),
                (be, ctx) -> be);
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.EAEP_CRAFTING_UNIT.get(),
                (be, ctx) -> be);
    }

    @SubscribeEvent
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModUpgradeCards.init();

            cellHandler();
            locator();
            partModel();
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
        ModBlocks.WIRELESS_TRANSCEIVER.get().setBlockEntity(
                BlockEntityWirelessTransceiver.class,
                ModBlockEntities.WIRELESS_TRANSCEIVER.get(),
                null, null);
        ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE.get().setBlockEntity(
                UploadCoreBlockEntity.class,
                ModBlockEntities.UPLOAD_CORE.get(),
                null, null);

        Arrays.stream(EAEPCraftingUnitType.values()).forEach(unit ->
                unit.getBlock().get().setBlockEntity(
                        CraftingBlockEntity.class,
                        ModBlockEntities.EAEP_CRAFTING_UNIT.get(),
                        null, null));

        AEBaseBlockEntity.registerBlockEntityItem(
                ModBlockEntities.WIRELESS_TRANSCEIVER.get(), ModItems.WIRELESS_TRANSCEIVER.get());
        AEBaseBlockEntity.registerBlockEntityItem(
                ModBlockEntities.UPLOAD_CORE.get(), ModItems.ASSEMBLER_MATRIX_UPLOAD_CORE.get());
    }
}
