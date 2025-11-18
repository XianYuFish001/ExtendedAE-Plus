package com.extendedae_plus.common.item;

import appeng.items.materials.UpgradeCardItem;
import com.extendedae_plus.common.dataComponent.DataSpeedCard;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.UtilGetKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 单一的实体加速卡 Item，通过 ItemStack 的 NBT 存储 exponent（0/1/2/3）来区分等级
 */
public class EntitySpeedCardItem extends UpgradeCardItem {
    public EntitySpeedCardItem(int multiplier) {
        super(new Properties()
                .component(ModDataComponents.DATA_SPEED_CARD, new DataSpeedCard(multiplier)));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return new UtilGetKey("item.%s.entity_speed_card%s")
                .addStr("multiplier")
                .args(DataSpeedCard.fromStack(stack))
                .build();
    }

    public List<Component> getTooltipLines(ItemStack stack) {
        var multiplier = DataSpeedCard.fromStack(stack);
        return List.of(
                new UtilGetKey(UtilGetKey.tooltip)
                        .item(ModItems.ENTITY_SPEED_CARD)
                        .addStr("multiplier")
                        .args(multiplier)
                        .build(),
                new UtilGetKey(UtilGetKey.tooltip)
                        .item(ModItems.ENTITY_SPEED_CARD)
                        .addStr("max")
                        .args((switch (multiplier) {
                            case 16 -> 1024;
                            case 8 -> 256;
                            case 4 -> 64;
                            case 2 -> 8;
                            default -> 1;
                        })).build()
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        lines.addAll(this.getTooltipLines(stack));
    }
}


