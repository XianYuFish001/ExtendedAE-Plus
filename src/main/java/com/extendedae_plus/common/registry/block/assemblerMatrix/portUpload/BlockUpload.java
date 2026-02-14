package com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockUpload extends BlockAssemblerMatrixBase<BlockEntityUpload> {
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public BlockUpload() {
        this.registerDefaultState(this.defaultBlockState()
                .setValue(LOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOCKED);
    }

    @Override
    public Item getPresentItem() {
        return ModItems.PortUpload.get();
    }

    @Override
    public void openGui(BlockEntityUpload tile, Player player) {
        MenuOpener.open(ModMenuTypes.LabelLinkManageable.get(), player, MenuLocators.forBlockEntity(tile));
    }
}
