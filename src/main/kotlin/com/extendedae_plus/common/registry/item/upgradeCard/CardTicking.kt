package com.extendedae_plus.common.registry.item.upgradeCard

import appeng.items.materials.UpgradeCardItem
import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import com.fish.fishlib.util.keyBuilder.toKeyPattern
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

/**
 * 单一的实体加速卡 Item，通过 ItemStack 的 NBT 存储 exponent（0/1/2/3）来区分等级
 */
class CardTicking(multiplier: Int, maxMultiplier: Int) : UpgradeCardItem(
    Properties()
        .component(EAEPDataComponents.CardTicking, DataTickingCard(multiplier, maxMultiplier))
) {
    override fun getName(stack: ItemStack): Component {
        return UtilKeyBuilder.of("item.%s.card_ticking%s".toKeyPattern())
            .addStr("multiplier")
            .args(DataTickingCard.fromStack(stack).multiplier)
            .build()
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        lines: MutableList<Component>,
        advancedTooltips: TooltipFlag
    ) {
        super.appendHoverText(stack, context, lines, advancedTooltips)

        DataTickingCard.fromStack(stack).let {
            UtilKeyBuilder.of(Patterns.Tooltip)
                .item(EAEPItems.CardTicking)
                .bindCollection(lines)
                .addStr("multiplier")
                .args(it.multiplier)
                .buildInto()
                .addStr("max")
                .args(it.maxMultiplier)
                .buildInto()
        }
    }
}


