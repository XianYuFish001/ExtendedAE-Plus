package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedSpeed;

import com.extendedae_plus.common.init.ModItems;
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase;
import net.minecraft.world.item.Item;

public class BlockAdvancedSpeed extends BlockAssemblerMatrixBase<BlockEntityAdvancedSpeed> {
    @Override
    public Item getPresentItem() {
        return ModItems.CoreAdvancedSpeed.get();
    }
}
