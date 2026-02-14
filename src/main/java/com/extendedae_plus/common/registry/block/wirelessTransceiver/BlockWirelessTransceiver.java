package com.extendedae_plus.common.registry.block.wirelessTransceiver;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.common.init.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull InteractionResult useWithoutItem(BlockState state,
                                                     Level level,
                                                     BlockPos pos,
                                                     Player player,
                                                     BlockHitResult hitResult) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return super.useWithoutItem(state, level, pos, player, hitResult);

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        MenuOpener.open(ModMenuTypes.LabelLinkManageable.get(), player,
                MenuLocators.forBlockEntity(blockEntity));
        return InteractionResult.CONSUME;
    }
}
