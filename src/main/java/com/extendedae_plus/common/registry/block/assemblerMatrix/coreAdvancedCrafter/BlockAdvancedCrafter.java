package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter;

import com.extendedae_plus.common.init.ModItems;
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase;
import net.minecraft.world.item.Item;

public class BlockAdvancedCrafter extends BlockAssemblerMatrixBase<BlockEntityAdvancedCrafter> {
    @Override
    public Item getPresentItem() {
        return ModItems.CoreAdvancedCrafter.get();
    }
}
