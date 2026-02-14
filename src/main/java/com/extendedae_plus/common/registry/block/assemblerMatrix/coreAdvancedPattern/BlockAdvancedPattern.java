package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern;

import com.extendedae_plus.common.init.ModItems;
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase;
import net.minecraft.world.item.Item;

public class BlockAdvancedPattern extends BlockAssemblerMatrixBase<BlockEntityAdvancedPattern> {
    @Override
    public Item getPresentItem() {
        return ModItems.CoreAdvancedPattern.get();
    }
}
