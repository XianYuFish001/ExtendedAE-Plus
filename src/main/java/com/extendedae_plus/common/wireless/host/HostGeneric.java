package com.extendedae_plus.common.wireless.host;

import appeng.api.networking.IGridNode;
import com.extendedae_plus.common.wireless.linkApi.ILinkHost;
import com.extendedae_plus.common.wireless.linkApi.Label;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public final class HostGeneric implements ILinkHost {
    private final Supplier<BlockEntity> getterBlockEntity;
    private final Supplier<IGridNode> getterNode;

    private Label label = Label.EMPTY;
    private @Nullable UUID placer = null;
    @Getter
    @Setter
    private String placerName = "";

    public HostGeneric(Supplier<BlockEntity> getterBlockEntity,
                       Supplier<IGridNode> getterNode) {
        this.getterBlockEntity = getterBlockEntity;
        this.getterNode = getterNode;
    }

    @Override
    public @Nullable ServerLevel getServerLevel() {
        var blockEntity = this.getterBlockEntity.get();
        if (blockEntity == null) return null;
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel)
            return serverLevel;
        else return null;
    }

    @Override
    public BlockPos getBlockPos() {
        var blockEntity = this.getterBlockEntity.get();
        if (blockEntity == null) return BlockPos.ZERO;
        return blockEntity.getBlockPos();
    }

    @Override
    public IGridNode getGridNode() {
        return this.getterNode.get();
    }

    @Override
    public boolean isRemoved() {
        var blockEntity = this.getterBlockEntity.get();
        return blockEntity == null || blockEntity.isRemoved();
    }

    @Override
    public Label getLabel() {
        return this.label;
    }

    @Override
    public @Nullable UUID getPlacer() {
        return this.placer;
    }

    @Override
    public void setPlacer(@Nullable UUID placer) {
        this.placer = placer;
    }

    @Override
    public void setLabel(Label label) {
        this.label = label;
    }

}
