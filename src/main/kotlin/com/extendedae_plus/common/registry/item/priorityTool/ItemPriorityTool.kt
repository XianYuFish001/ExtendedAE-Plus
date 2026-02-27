package com.extendedae_plus.common.registry.item.priorityTool

import appeng.api.implementations.menuobjects.IMenuItem
import appeng.blockentity.networking.CableBusBlockEntity
import appeng.helpers.IPriorityHost
import appeng.items.AEBaseItem
import appeng.menu.MenuOpener
import appeng.menu.locator.ItemMenuHostLocator
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.extendedae_plus.common.registry.menu.host.HostPriorityTool
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.extension.invoke
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

class ItemPriorityTool : AEBaseItem(Properties().stacksTo(1)), IMenuItem {
    override fun onItemUseFirst(stack: ItemStack, context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.FAIL
        val level = context.level as? ServerLevel ?: return InteractionResult.sidedSuccess(true)

        val openMenu = {
            MenuOpener.open(
                EAEPMenuTypes.PriorityTool(), player,
                MenuLocators.forHand(player, context.hand)
            )
            InteractionResult.CONSUME
        }

        val data = stack.get(EAEPDataComponents.Priority) ?: return openMenu()
        val tile = level.getBlockEntity(context.clickedPos) ?: return openMenu()

        val priorityHost = when (tile) {
            is IPriorityHost -> tile
            is CableBusBlockEntity -> tile.selectPartWorld(context.clickLocation)?.part as? IPriorityHost
            else -> null
        } ?: return openMenu()

        priorityHost.priority = data.priority
        stack.set(EAEPDataComponents.Priority, data.apply())
        return InteractionResult.CONSUME
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val item = player.getItemInHand(usedHand)

        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(item, true)

        if (
            !MenuOpener.open(
                EAEPMenuTypes.PriorityTool(),
                player,
                MenuLocators.forHand(player, usedHand)
            )
        ) return InteractionResultHolder.fail(item)

        return InteractionResultHolder.consume(item)
    }

    override fun getMenuHost(
        player: Player, locator: ItemMenuHostLocator, hitResult: BlockHitResult?
    ) = HostPriorityTool(this, player, locator)

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(
            UtilKeyBuilder.of(Patterns.Tooltip)
                .item(EAEPItems.PriorityTool)
                .build()
        )
    }
}
