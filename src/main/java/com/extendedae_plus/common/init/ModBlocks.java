package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.registry.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter.BlockAdvancedCrafter;
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern.BlockAdvancedPattern;
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedSpeed.BlockAdvancedSpeed;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCK =
            DeferredRegister.createBlocks(ExtendedAEPlus.MODID);

    public static final DeferredBlock<BlockWirelessTransceiver> WIRELESS_TRANSCEIVER =
            BLOCK.register("wireless_transceiver", BlockWirelessTransceiver::new);
    public static final DeferredBlock<BlockUpload> PORT_UPLOAD =
            BLOCK.register("assembler_matrix_upload", BlockUpload::new);
    public static final DeferredBlock<BlockAdvancedCrafter> CORE_ADVANCED_CRAFTER =
            BLOCK.register("assembler_matrix_advanced_crafter", BlockAdvancedCrafter::new);
    public static final DeferredBlock<BlockAdvancedPattern> CORE_ADVANCED_PATTERN =
            BLOCK.register("assembler_matrix_advanced_pattern", BlockAdvancedPattern::new);
    public static final DeferredBlock<BlockAdvancedSpeed> CORE_ADVANCED_SPEED =
            BLOCK.register("assembler_matrix_advanced_speed", BlockAdvancedSpeed::new);

    /// CraftingUnit Blocks are now in {@link EAEPCraftingUnitType#UNIT_BLOCKS}
    static {
        EAEPCraftingUnitType.init();
    }
}
