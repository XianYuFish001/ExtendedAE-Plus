package com.extendedae_plus.common.block.wirelessTransceiver;

import appeng.block.AEBaseEntityBlock;
import appeng.core.definitions.AEItems;
import appeng.util.InteractionUtil;
import com.extendedae_plus.client.screen.FrequencyInputScreen;
import com.extendedae_plus.common.dataComponent.DataChannelCard;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockWirelessTransceiver extends AEBaseEntityBlock<BlockEntityWirelessTransceiver> {
    public static final BooleanProperty MASTER_MODE = BooleanProperty.create("master_mode");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public BlockWirelessTransceiver() {
        super(metalProps());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MASTER_MODE, false)
                .setValue(POWERED, false)
                .setValue(LOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MASTER_MODE, POWERED, LOCKED);
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
            if (!(level.getBlockEntity(pos) instanceof BlockEntityWirelessTransceiver blockEntity)) return;
            if (!level.isClientSide()) return;
            FrequencyInputScreen.open(pos, blockEntity.getFrequency());
        } else super.attack(state, level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack,
                                              BlockState state,
                                              Level level,
                                              BlockPos pos,
                                              Player player,
                                              InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND
                || !(level.getBlockEntity(pos) instanceof BlockEntityWirelessTransceiver blockEntity))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean shift = player.isShiftKeyDown();
        if (stack.is(ModItems.CHANNEL_CARD)) {
            DataChannelCard.copyFromTransceiver(blockEntity, stack);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        } else if (stack.is(AEItems.MEMORY_CARD.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        } else if (state.getValue(LOCKED)) {
            player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.actionBar)
                            .item(ModItems.WIRELESS_TRANSCEIVER)
                            .addStr("locked")
                            .build(),
                    true);
            return ItemInteractionResult.FAIL;
        } else if (InteractionUtil.canWrenchDisassemble(stack)) {
            if (!shift) {
                var newState = state.setValue(MASTER_MODE, !state.getValue(MASTER_MODE));
                level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
                player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.actionBar)
                                .item(ModItems.WIRELESS_TRANSCEIVER)
                                .addStr("mode")
                                .addStr(!state.getValue(MASTER_MODE), "master", "slave")
                                .build(),
                        true);
                blockEntity.onSwitchMode();
            } else blockEntity.disassembleWithWrench(player, level, hitResult, stack);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
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
