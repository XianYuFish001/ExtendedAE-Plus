package com.extendedae_plus.common.item;

import appeng.items.parts.PartItem;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.part.ticker.PartTicker;
import com.extendedae_plus.util.UtilGetKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemTicker extends PartItem<PartTicker> {
    public ItemTicker() {
        super(new Properties(), PartTicker.class, PartTicker::new);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
        tooltip.add(new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.PART_TICKER)
                .addStr(tooltipFlag.hasShiftDown(), "advanced_tip")
                .build());
    }
}
