package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.registry.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter.BlockAdvancedCrafter;
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern.BlockAdvancedPattern;
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedSpeed.BlockAdvancedSpeed;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/// CraftingUnit Blocks are now in {@link EAEPCraftingUnitType#UNIT_BLOCKS}
public final class ModBlocks {
    @InitObject
    public static final DeferredRegister.Blocks BLOCK =
            DeferredRegister.createBlocks(ExtendedAEPlus.MODID);

    public static final List<DeferredBlock<? extends Block>> BLOCKS = new ArrayList<>();

    public static final DeferredBlock<BlockWirelessTransceiver> WIRELESS_TRANSCEIVER =
            register("wireless_transceiver", BlockWirelessTransceiver::new);
    public static final DeferredBlock<BlockUpload> PORT_UPLOAD =
            register("assembler_matrix_upload", BlockUpload::new);
    public static final DeferredBlock<BlockAdvancedCrafter> CORE_ADVANCED_CRAFTER =
            register("assembler_matrix_advanced_crafter", BlockAdvancedCrafter::new);
    public static final DeferredBlock<BlockAdvancedPattern> CORE_ADVANCED_PATTERN =
            register("assembler_matrix_advanced_pattern", BlockAdvancedPattern::new);
    public static final DeferredBlock<BlockAdvancedSpeed> CORE_ADVANCED_SPEED =
            register("assembler_matrix_advanced_speed", BlockAdvancedSpeed::new);

    public static <TBlock extends Block> DeferredBlock<TBlock> register(
            String name, Supplier<TBlock> instanter
    ) {
        var block = BLOCK.register(name, instanter);
        BLOCKS.add(block);
        return block;
    }
}