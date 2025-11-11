package com.extendedae_plus.common.event;

import appeng.api.parts.IPart;
import appeng.api.parts.PartModels;
import appeng.api.storage.StorageCells;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.ExtendedAEPlus;
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

                AEBaseEntityBlock<CraftingBlockEntity> b4 = ModBlocks.ACCELERATOR_4x.get();
                AEBaseEntityBlock<CraftingBlockEntity> b16 = ModBlocks.ACCELERATOR_16x.get();
                AEBaseEntityBlock<CraftingBlockEntity> b64 = ModBlocks.ACCELERATOR_64x.get();
                AEBaseEntityBlock<CraftingBlockEntity> b256 = ModBlocks.ACCELERATOR_256x.get();
                AEBaseEntityBlock<CraftingBlockEntity> b1024 = ModBlocks.ACCELERATOR_1024x.get();

                // 使用我们自定义的 CraftingBlockEntity 类型，它的有效方块列表包含自定义加速器
                var type = ModBlockEntities.EPLUS_CRAFTING_UNIT_BE.get();
                // 不提供专用 ticker（AE2 会在其注册时按接口注入），此处传 null 即可
                b4.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                b16.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                b64.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                b256.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                b1024.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                LOGGER.info("Bound AE2 CraftingBlockEntity to ExtendedAE Plus accelerators.");

                // 绑定装配矩阵上传核心方块实体类型，避免 blockEntityClass 为 null 的问题
                ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE.get().setBlockEntity(
                        com.extendedae_plus.common.block.uploadCore.UploadCoreBlockEntity.class,
                        ModBlockEntities.UPLOAD_CORE_BE.get(),
                        null,
                        null
                );
                LOGGER.info("Bound UploadCoreBlockEntity to assembler matrix upload core block.");
            } catch (Throwable t) {
                LOGGER.warn("Common Initialize failed ", t);
            }
        });
    }
}
