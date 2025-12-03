package com.extendedae_plus.common.block.assemblerMatrix.coreUpload;

import com.extendedae_plus.common.init.ModBlockEntities;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ExtendedAE_Plus: 装配矩阵上传核心方块实体。
 * 作为矩阵内部功能块，仅用于标记该矩阵允许被自动上传（工具类会在集群中查找此实体）。
 */
public class UploadCoreBlockEntity extends TileAssemblerMatrixBase {
    public UploadCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CORE_UPLOAD.get(), pos, state);
    }
}
