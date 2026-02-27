package com.extendedae_plus.common.registry.item.upgradeCard

import appeng.items.materials.UpgradeCardItem
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class CardAutoCompletion : UpgradeCardItem(Properties()) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        lines: MutableList<Component>,
        advancedTooltips: TooltipFlag
    ) {
        super.appendHoverText(stack, context, lines, advancedTooltips)
        lines.add(
            UtilKeyBuilder.of(Patterns.Tooltip)
                .item(EAEPItems.CardAutoCompletion)
                .addStr(advancedTooltips.hasShiftDown(), "advanced_tip")
                .build()
        )
    }
}
