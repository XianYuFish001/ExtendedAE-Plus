package com.extendedae_plus.common.registry.item.upgradeCard;

import appeng.items.materials.UpgradeCardItem;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.keyBuilder.Patterns;
import com.extendedae_plus.util.keyBuilder.UtilKeyBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CardAutoCompletion extends UpgradeCardItem {
    public CardAutoCompletion() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        lines.add(UtilKeyBuilder.of(Patterns.tooltip)
                .item(ModItems.CardAutoCompletion)
                .addStr(advancedTooltips.hasShiftDown(), "advanced_tip")
                .build()
        );
    }
}
