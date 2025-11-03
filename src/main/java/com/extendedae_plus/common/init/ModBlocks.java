package com.extendedae_plus.common.init;

import appeng.block.crafting.CraftingUnitBlock;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.block.uploadCore.UploadCoreBlock;
import com.extendedae_plus.common.block.wirelessTransceiver.WirelessTransceiverBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExtendedAEPlus.MODID);

    public static final DeferredBlock<Block> WIRELESS_TRANSCEIVER = BLOCKS.register(
            "wireless_transceiver",
            () -> new WirelessTransceiverBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
            )
    );

    // AE2 网络模式控制器方块
    public static final DeferredBlock<Block> NETWORK_PATTERN_CONTROLLER = BLOCKS.register(
            "network_pattern_controller",
            () -> new com.extendedae_plus.common.block.patternController.NetworkPatternControllerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
            )
    );

    // Crafting Accelerators (reuse MAE2 textures/models)
    public static final DeferredBlock<CraftingUnitBlock> ACCELERATOR_4x = BLOCKS.register(
            "4x_crafting_accelerator",
            () -> new CraftingUnitBlock(EAEPCraftingUnitType.ACCELERATOR_4x)
    );

    public static final DeferredBlock<CraftingUnitBlock> ACCELERATOR_16x = BLOCKS.register(
            "16x_crafting_accelerator",
            () -> new CraftingUnitBlock(EAEPCraftingUnitType.ACCELERATOR_16x)
    );

    public static final DeferredBlock<CraftingUnitBlock> ACCELERATOR_64x = BLOCKS.register(
            "64x_crafting_accelerator",
            () -> new CraftingUnitBlock(EAEPCraftingUnitType.ACCELERATOR_64x)
    );

    public static final DeferredBlock<CraftingUnitBlock> ACCELERATOR_256x = BLOCKS.register(
            "256x_crafting_accelerator",
            () -> new CraftingUnitBlock(EAEPCraftingUnitType.ACCELERATOR_256x)
    );

    public static final DeferredBlock<CraftingUnitBlock> ACCELERATOR_1024x = BLOCKS.register(
            "1024x_crafting_accelerator",
            () -> new CraftingUnitBlock(EAEPCraftingUnitType.ACCELERATOR_1024x)
    );

    // 装配矩阵上传核心方块
    public static final DeferredBlock<UploadCoreBlock> ASSEMBLER_MATRIX_UPLOAD_CORE = BLOCKS.register(
            "assembler_matrix_upload_core",
            () -> new UploadCoreBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
            )
    );
}
