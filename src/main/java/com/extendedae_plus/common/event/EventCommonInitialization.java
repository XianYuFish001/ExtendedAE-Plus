package com.extendedae_plus.common.event;

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
import com.extendedae_plus.common.init.UpgradeCards;
import com.extendedae_plus.common.item.infinityBigIntegerCell.InfinityBigIntegerCellHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

import java.util.Arrays;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class EventCommonInitialization {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    private static void commonSetup(FMLCommonSetupEvent event) {
        // 出现了! 谜一样的log
//        LOGGER.info("HELLO FROM COMMON SETUP");
//        LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        StorageCells.addCellHandler(InfinityBigIntegerCellHandler.INSTANCE);

        // 绑定 AE2 的 CraftingBlockEntity 到本模组的自定义加速器方块，避免 AEBaseEntityBlock.blockEntityType 为空
        event.enqueueWork(() -> {
            try {
                // 注册升级卡
                UpgradeCards.init();

                MenuLocators.register(
                        CuriosItemLocator.class,
                        CuriosItemLocator::writeToPacket,
                        CuriosItemLocator::readFromPacket
                );

                // 为 PartItem 注册 AE2 部件模型
                PartModels.registerModels(
                        PartModelsHelper.createModels(
                                ModItems.ENTITY_TICKER_PART_ITEM.get().getPartClass().asSubclass(IPart.class)
                        )
                );

                ModBlocks.WIRELESS_TRANSCEIVER.get().setBlockEntity(
                        BlockEntityWirelessTransceiver.class,
                        ModBlockEntities.WIRELESS_TRANSCEIVER.get(),
                        null, null
                );

                AEBaseBlockEntity.registerBlockEntityItem(ModBlockEntities.WIRELESS_TRANSCEIVER.get(), ModItems.WIRELESS_TRANSCEIVER.get());

                // 绑定装配矩阵上传核心方块实体类型，避免 blockEntityClass 为 null 的问题
                ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE.get().setBlockEntity(
                        UploadCoreBlockEntity.class,
                        ModBlockEntities.UPLOAD_CORE.get(),
                        null,
                        null
                );

                AEBaseBlockEntity.registerBlockEntityItem(ModBlockEntities.UPLOAD_CORE.get(), ModItems.ASSEMBLER_MATRIX_UPLOAD_CORE.get());

                Arrays.stream(EAEPCraftingUnitType.values()).forEach(unit ->
                        unit.getBlock().get().setBlockEntity(CraftingBlockEntity.class,
                                ModBlockEntities.EAEP_CRAFTING_UNIT.get(),
                                null, null));
            } catch (Throwable t) {
                LOGGER.warn("Common Initialize failed ", t);
            }
        });
    }
}
