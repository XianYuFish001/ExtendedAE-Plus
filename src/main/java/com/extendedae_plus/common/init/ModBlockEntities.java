package com.extendedae_plus.common.init;

import appeng.block.crafting.CraftingUnitBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.block.uploadCore.UploadCoreBlockEntity;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExtendedAEPlus.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityWirelessTransceiver>> WIRELESS_TRANSCEIVER =
            BLOCK_ENTITY_TYPES.register("wireless_transceiver",
                    () -> BlockEntityType.Builder.of(BlockEntityWirelessTransceiver::new,
                            ModBlocks.WIRELESS_TRANSCEIVER.get()).build(null));

    // 提供一个 CraftingBlockEntity 的类型，允许附着在本模组自定义加速器方块上，绕过 AE2 默认类型的“有效方块列表”校验
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingBlockEntity>> EAEP_CRAFTING_UNIT =
            BLOCK_ENTITY_TYPES.register("eaep_crafting_unit",
                    () -> {
                        AtomicReference<BlockEntityType<CraftingBlockEntity>> ref = new AtomicReference<>();
                        BlockEntityType<CraftingBlockEntity> type = BlockEntityType.Builder.of(
                                (pos, state) -> new CraftingBlockEntity(ref.get(), pos, state),
                                Arrays.stream(EAEPCraftingUnitType.values())
                                        .map(unit -> unit.getBlock().get())
                                        .toArray(CraftingUnitBlock[]::new)
                        ).build(null);
                        ref.set(type);
                        return type;
                    });

    // 装配矩阵上传核心方块实体
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UploadCoreBlockEntity>> UPLOAD_CORE =
            BLOCK_ENTITY_TYPES.register("upload_core",
                    () -> BlockEntityType.Builder.of(UploadCoreBlockEntity::new,
                            ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE.get()).build(null));
}
