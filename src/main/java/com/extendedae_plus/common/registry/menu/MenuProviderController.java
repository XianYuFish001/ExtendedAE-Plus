package com.extendedae_plus.common.registry.menu;

import com.extendedae_plus.common.init.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MenuProviderController extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Direction clickedFace;

    public MenuProviderController(int id, Inventory inv, @Nullable FriendlyByteBuf buf) {
        this(id, inv,
                buf != null ? buf.readBlockPos() : BlockPos.ZERO,
                buf != null ? Direction.byName(buf.readUtf()) : Direction.UP);
    }

    public MenuProviderController(int id, Inventory inv, BlockPos pos, Direction face) {
        super(ModMenuTypes.providerController.get(), id);
        this.pos = pos;
        this.clickedFace = face;
    }

    public BlockPos getBlockEntityPos() {
        return pos;
    }

    public Direction getClickedFace() {
        return clickedFace;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
