package com.extendedae_plus.common.registry.item.upgradeCard

import appeng.api.implementations.menuobjects.IMenuItem
import appeng.api.upgrades.Upgrades
import appeng.blockentity.networking.CableBusBlockEntity
import appeng.items.materials.UpgradeCardItem
import appeng.menu.MenuOpener
import appeng.menu.locator.ItemMenuHostLocator
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.extendedae_plus.common.registry.dataComponent.DataChannelCard
import com.extendedae_plus.common.registry.menu.host.link.HostCardChannel
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.extension.invoke
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

/**
 * 频道卡：存储频道号、所有者UUID和团队信息
 * - 右键空气：增加频道号
 * - 潜行右键空气：减少频道号
 * - 潜行左键空气：写入/清除玩家UUID和团队信息（通过网络包处理）
 * - 潜行左键收发器：将频道卡的所有者信息写入收发器
 * 继承 AE2 的 UpgradeCardItem 以复用升级卡判定与提示框架。
 */
class CardChannel : UpgradeCardItem(Properties()), IMenuItem {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        lines: MutableList<Component>,
        flag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, lines, flag)

        // 显示频道
        val label = DataChannelCard.getLabel(stack)
        lines.add(
            UtilKeyBuilder.of(Patterns.Tooltip)
                .item(EAEPItems.CardChannel)
                .addStr("label")
                .addStr(label.isEmpty, "unset")
                .args(label.displayValue, label.description().string)
                .build()
        )

        // 显示所有者信息
        val ownerUUID = DataChannelCard.getOwnerUUID(stack)
        val nameOwner = DataChannelCard.getOwnerName(stack)

        lines.add(
            UtilKeyBuilder.of(Patterns.Tooltip)
                .item(EAEPItems.CardChannel)
                .addStr(!nameOwner.isEmpty(), "name")
                .addStr(nameOwner.isEmpty() && ownerUUID != null, "id")
                .args(nameOwner, ownerUUID?.toString()?.substring(0, 8))
                .build()
        )
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (level.isClientSide()) return InteractionResultHolder.success(stack)

        MenuOpener.open(EAEPMenuTypes.LabelLink(), player, MenuLocators.forHand(player, hand))
        return InteractionResultHolder.consume(stack)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val tile = context.level.getBlockEntity(context.clickedPos)

        val currentBlock = if (tile is CableBusBlockEntity) {
            tile.selectPartWorld(context.clickLocation).part.partItem
        } else {
            context.level
                .getBlockState(context.clickedPos)
                .block
                .asItem()
        }

        val available = Upgrades.getMaxInstallable(EAEPItems.CardChannel, currentBlock) > 0
        return if (available) InteractionResult.FAIL
        else super.useOn(context)
    }

    override fun onItemUseFirst(stack: ItemStack, context: UseOnContext): InteractionResult {
        val emptyOwner = DataChannelCard.getOwnerUUID(stack) == null
        val player = context.player
        if (!emptyOwner
            || context.level.isClientSide()
            || player == null
            || !player.isShiftKeyDown
        ) return super.onItemUseFirst(stack, context)

        DataChannelCard.setOwner(stack, player.getUUID(), player.name.string)
        val result = super.onItemUseFirst(stack, context)
        DataChannelCard.clearOwner(player.getItemInHand(context.hand))
        return result
    }

    override fun getMenuHost(
        player: Player,
        locator: ItemMenuHostLocator,
        hitResult: BlockHitResult?
    ) = HostCardChannel(this, player, locator)
}
