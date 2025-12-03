package com.extendedae_plus.common.block.assemblerMatrix.coreAdvancedSpeed;

import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.mixin.impl.bridge.HelperAssemblerMatrixModifier;
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixSpeed;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityAdvancedSpeed extends TileAssemblerMatrixSpeed {
    public BlockEntityAdvancedSpeed(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    public void add(ClusterAssemblerMatrix cluster) {
        for (int i = 0; i < 3; i++)
            ((HelperAssemblerMatrixModifier) cluster).eaep$addSpeedCore();
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.CORE_ADVANCED_SPEED.get();
    }
}
