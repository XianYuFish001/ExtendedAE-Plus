package com.extendedae_plus.common.init;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.block.AEBaseEntityBlock;
import appeng.block.crafting.CraftingUnitBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.block.assemblerMatrix.coreAdvancedCrafter.BlockEntityAdvancedCrafter;
import com.extendedae_plus.common.block.assemblerMatrix.coreAdvancedPattern.BlockEntityAdvancedPattern;
import com.extendedae_plus.common.block.assemblerMatrix.coreAdvancedSpeed.BlockEntityAdvancedSpeed;
import com.extendedae_plus.common.block.assemblerMatrix.coreUpload.UploadCoreBlockEntity;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExtendedAEPlus.MODID);

    public static final List<InfoBlockEntity<? extends BlockEntity>> BLOCK_ENTITIES = new ArrayList<>();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityWirelessTransceiver>> WIRELESS_TRANSCEIVER =
            regCommonBlockEntity(BlockEntityWirelessTransceiver.class,
                    BlockEntityWirelessTransceiver::new,
                    ModBlocks.WIRELESS_TRANSCEIVER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UploadCoreBlockEntity>> CORE_UPLOAD =
            regCommonBlockEntity(UploadCoreBlockEntity.class,
                    UploadCoreBlockEntity::new,
                    ModBlocks.CORE_UPLOAD);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityAdvancedCrafter>> CORE_ADVANCED_CRAFTER =
            regCommonBlockEntity(BlockEntityAdvancedCrafter.class,
                    BlockEntityAdvancedCrafter::new,
                    ModBlocks.CORE_ADVANCED_CRAFTER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityAdvancedPattern>> CORE_ADVANCED_PATTERN =
            regCommonBlockEntity(BlockEntityAdvancedPattern.class,
                    BlockEntityAdvancedPattern::new,
                    ModBlocks.CORE_ADVANCED_PATTERN);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityAdvancedSpeed>> CORE_ADVANCED_SPEED =
            regCommonBlockEntity(BlockEntityAdvancedSpeed.class,
                    BlockEntityAdvancedSpeed::new,
                    ModBlocks.CORE_ADVANCED_SPEED);

    // 提供一个 CraftingBlockEntity 的类型，允许附着在本模组自定义加速器方块上，绕过 AE2 默认类型的“有效方块列表”校验
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingBlockEntity>> EAEP_CRAFTING_UNIT =
            BLOCK_ENTITY_TYPE.register("eaep_crafting_unit",
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

    public static void onCapabilitiesRegistering(RegisterCapabilitiesEvent event) {
        List.of(
                new InfoCapability<>(IInWorldGridNodeHost.class, AECapabilities.IN_WORLD_GRID_NODE_HOST, null)
        ).forEach(infoCapability ->
                BLOCK_ENTITIES.forEach(infoBlockEntity ->
                        infoCapability.register(event, infoBlockEntity)));
    }

//    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onBlockEntityBinding() {
        BLOCK_ENTITIES.forEach(info -> {
            if (!AEBaseBlockEntity.class.isAssignableFrom(info.clazz)) return;
            Arrays.stream(info.blocks).forEach(block -> {
                if (!(block.get() instanceof AEBaseEntityBlock entityBlock)) return;
                entityBlock.setBlockEntity(info.clazz, info.holder.get(), null, null);
            });
            AEBaseBlockEntity.registerBlockEntityItem(info.holder.get(), info.blocks[0].asItem());
        });
    }

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>>
    regCommonBlockEntity(Class<T> clazzBlockEntity, BlockEntityType.BlockEntitySupplier<T> factory,
                         DeferredBlock<?> firstBlock, DeferredBlock<?>... otherBlocks) {
        DeferredBlock<?>[] currentBlocks;
        if (otherBlocks.length > 0) {
            var blocks = Arrays.asList(otherBlocks);
            blocks.add(firstBlock);
            currentBlocks = blocks.toArray(DeferredBlock[]::new);
        } else currentBlocks = new DeferredBlock[]{firstBlock};

        var holder = BLOCK_ENTITY_TYPE.register(firstBlock.getId().getPath(),
                () -> BlockEntityType.Builder.of(
                        factory,
                        Arrays.stream(currentBlocks).map(DeferredBlock::get).toArray(Block[]::new)
                ).build(null));
        BLOCK_ENTITIES.add(new InfoBlockEntity<>(clazzBlockEntity, holder, firstBlock));
        return holder;
    }

    public record InfoBlockEntity<T extends BlockEntity>(
            Class<T> clazz,
            DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder,
            DeferredBlock<?>... blocks
    ) {
    }

    public record InfoCapability<THost extends BlockEntity, TCapability, TContext>(
            Class<TCapability> clazzCapability,
            BlockCapability<TCapability, TContext> capability,
            @Nullable ICapabilityProvider<THost, TContext, TCapability> provider
    ) {
        public void register(RegisterCapabilitiesEvent event, InfoBlockEntity<? extends THost> info) {
            if (!this.clazzCapability.isAssignableFrom(info.clazz)) return;

            var provider = this.provider;
            if (provider == null) provider = (blockEntity, context) ->
                    this.clazzCapability.cast(blockEntity);

            event.registerBlockEntity(this.capability, info.holder.get(), provider);
        }
    }
}
