package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.block.uploadCore.UploadCoreBlock;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockWirelessTransceiver;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCK =
            DeferredRegister.createBlocks(ExtendedAEPlus.MODID);

    public static final DeferredBlock<BlockWirelessTransceiver> WIRELESS_TRANSCEIVER =
            BLOCK.register("wireless_transceiver", BlockWirelessTransceiver::new);
    public static final DeferredBlock<UploadCoreBlock> ASSEMBLER_MATRIX_UPLOAD_CORE =
            BLOCK.register("assembler_matrix_upload_core", UploadCoreBlock::new);

    /// CraftingUnit Blocks are now in {@link EAEPCraftingUnitType#UNIT_BLOCKS}
    static {
        EAEPCraftingUnitType.init();
    }
}
