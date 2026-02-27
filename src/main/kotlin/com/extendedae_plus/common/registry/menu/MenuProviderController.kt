package com.extendedae_plus.common.registry.menu

import com.extendedae_plus.common.init.EAEPMenuTypes
import com.fish.fishlib.util.extension.invoke
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class MenuProviderController(
    id: Int,
    inv: Inventory,
    val blockEntityPos: BlockPos,
    val clickedFace: Direction
) : AbstractContainerMenu(EAEPMenuTypes.ControllerProvider(), id) {
    constructor(id: Int, inv: Inventory, buf: FriendlyByteBuf?) : this(
        id, inv,
        if (buf != null) buf.readBlockPos() else BlockPos.ZERO,
        if (buf != null) Direction.byName(buf.readUtf())!! else Direction.UP
    )

    override fun stillValid(player: Player) = true

    override fun quickMoveStack(player: Player, index: Int) = ItemStack.EMPTY
}
