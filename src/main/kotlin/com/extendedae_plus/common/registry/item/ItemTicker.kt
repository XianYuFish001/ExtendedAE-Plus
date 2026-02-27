package com.extendedae_plus.common.registry.item

import appeng.items.parts.PartItem
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.registry.part.ticker.PartTicker
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class ItemTicker : PartItem<PartTicker>(
    Properties(),
    PartTicker::class.java,
    ::PartTicker
) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
        tooltip.add(
            UtilKeyBuilder.of(Patterns.Tooltip)
                .item(EAEPItems.Ticker)
                .addStr(tooltipFlag.hasShiftDown(), "advanced_tip")
                .build()
        )
    }
}
