package com.extendedae_plus.common.block.assemblerMatrix.portUpload;

import appeng.core.definitions.AEItems;
import appeng.util.InteractionUtil;
import com.extendedae_plus.client.screen.FrequencyInputScreen;
import com.extendedae_plus.common.api.IBlockEntityFrequency;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

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
        return ModItems.PORT_UPLOAD.get();
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void openGui(BlockEntityUpload tile, Player p) {
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (InteractionUtil.canWrenchDisassemble(player.getMainHandItem())) {
            var newState = state.setValue(LOCKED, !state.getValue(LOCKED));
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
            player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.actionBar)
                            .item(ModItems.WIRELESS_TRANSCEIVER)
                            .addStr(!state.getValue(LOCKED), "switch_locked", "switch_unlock")
                            .build(),
                    true);
        } else if (player.isShiftKeyDown()) {
            if (!(level.getBlockEntity(pos) instanceof IBlockEntityFrequency blockEntity)) return;
            if (!level.isClientSide()) return;
            FrequencyInputScreen.open(pos, blockEntity.getFrequency());
        } else super.attack(state, level, pos, player);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack,
                                              BlockState state,
                                              Level level,
                                              BlockPos pos,
                                              Player player,
                                              InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND
                || !(level.getBlockEntity(pos) instanceof IBlockEntityFrequency blockEntity))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean shift = player.isShiftKeyDown();
        if (stack.is(AEItems.MEMORY_CARD.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        } else if (state.getValue(LOCKED)) {
            player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.actionBar)
                            .item(ModItems.WIRELESS_TRANSCEIVER)
                            .addStr("locked")
                            .build(),
                    true);
            return ItemInteractionResult.FAIL;
        }

        long step = shift ? -1 : 1;
        if (stack.is(Items.STICK) || stack.is(Items.REDSTONE_TORCH)) step *= 10;

        step = Math.max(step + blockEntity.getFrequency(), 0);
        blockEntity.setFrequency(step, false);

        player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.actionBar)
                        .item(ModItems.WIRELESS_TRANSCEIVER)
                        .addStr("frequency")
                        .addStr(blockEntity.getFrequency() == 0, "unset")
                        .args(blockEntity.getFrequency())
                        .build(),
                true);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
