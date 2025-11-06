package com.extendedae_plus;

import appeng.api.parts.IPart;
import appeng.api.parts.PartModels;
import appeng.api.storage.StorageCells;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.common.init.*;
import com.extendedae_plus.common.item.infinityBigIntegerCell.InfinityBigIntegerCellHandler;
import com.extendedae_plus.util.CuriosItemLocator;
import com.extendedae_plus.util.storage.InfinityStorageManager;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(ExtendedAEPlus.MODID)
public class ExtendedAEPlus {
    public static final String MODID = "extendedae_plus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExtendedAEPlus(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(ExtendedAEPlus::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ExtendedAEPlus::onServerStopped);

        modEventBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, EAEPConfig.COMMON_SPEC, "extendedae_plus/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, EAEPConfig.CLIENT_SPEC, "extendedae_plus/client.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, EAEPConfig.SERVER_SPEC, "extendedae_plus/server.toml");
    }

    public static ResourceLocation getLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 出现了! 谜一样的log
//        ExtendedAEPlus.LOGGER.info("HELLO FROM COMMON SETUP");
//        ExtendedAEPlus.LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        StorageCells.addCellHandler(InfinityBigIntegerCellHandler.INSTANCE);

        // 绑定 AE2 的 CraftingBlockEntity 到本模组的自定义加速器方块，避免 AEBaseEntityBlock.blockEntityType 为空
        event.enqueueWork(() -> {
            try {
                // 注册升级卡
                new UpgradeCards(event);

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
                ExtendedAEPlus.LOGGER.info("Bound AE2 CraftingBlockEntity to ExtendedAE Plus accelerators.");

                // 绑定装配矩阵上传核心方块实体类型，避免 blockEntityClass 为 null 的问题
                ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE.get().setBlockEntity(
                    com.extendedae_plus.common.block.uploadCore.UploadCoreBlockEntity.class,
                    ModBlockEntities.UPLOAD_CORE_BE.get(),
                    null,
                    null
                );
                ExtendedAEPlus.LOGGER.info("Bound UploadCoreBlockEntity to assembler matrix upload core block.");
            } catch (Throwable t) {
                ExtendedAEPlus.LOGGER.warn("Failed to bind block entities: {}", t.toString());
            }
        });
    }

    private static InfinityStorageManager storageManager;
    private static MinecraftServer storageManagerServer;

    private static void onServerStarted(ServerStartedEvent event) {
        storageManagerServer = event.getServer();
        storageManager = InfinityStorageManager.getInstance(event.getServer());
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        if (storageManagerServer == event.getServer()) {
            storageManagerServer = null;
            storageManager = null;
        }
    }

    @Nullable
    public static InfinityStorageManager currentStorageManager() {
        return storageManager;
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}

