package com.extendedae_plus.common.registry.item

import appeng.api.networking.IInWorldGridNodeHost
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.registry.menu.MenuProviderController
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

class ItemProviderController : Item(Properties().stacksTo(1)), MenuProvider {
    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        if (level.isClientSide()) return InteractionResult.sidedSuccess(true)

        val provider = context.itemInHand.item as? MenuProvider ?: return super.useOn(context)

        val player = context.player ?: return super.useOn(context)

        val tileRaw = level.getBlockEntity(context.clickedPos)
        if (tileRaw is IInWorldGridNodeHost) {
            player.openMenu(provider) { buffer ->
                buffer.writeBlockPos(context.clickedPos)
                buffer.writeUtf(context.clickedFace.getName())
            }
        }
        return InteractionResult.CONSUME
    }

    override fun getDisplayName() = UtilKeyBuilder.of(Patterns.Screen)
            .item(EAEPItems.ControllerProvider)
            .build()

    override fun createMenu(i: Int, inventory: Inventory, player: Player) =
        MenuProviderController(i, inventory, null)
}
