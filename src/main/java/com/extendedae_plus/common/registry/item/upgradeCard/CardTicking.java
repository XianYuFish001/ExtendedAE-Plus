package com.extendedae_plus.common.registry.item.upgradeCard;

import appeng.items.materials.UpgradeCardItem;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard;
import com.extendedae_plus.util.keyBuilder.Patterns;
import com.extendedae_plus.util.keyBuilder.PatternsKt;
import com.extendedae_plus.util.keyBuilder.UtilKeyBuilder;
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
public class CardTicking extends UpgradeCardItem {
    public CardTicking(int multiplier, int maxMultiplier) {
        super(new Properties()
                .component(ModDataComponents.CardTicking, new DataTickingCard(multiplier, maxMultiplier)));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return UtilKeyBuilder.of(PatternsKt.toKeyPattern("item.%s.card_ticking%s"))
                .addStr("multiplier")
                .args(DataTickingCard.fromStack(stack).multiplier())
                .build();
    }

    public List<Component> getTooltipLines(ItemStack stack) {
        var data = DataTickingCard.fromStack(stack);
        return List.of(
                UtilKeyBuilder.of(Patterns.tooltip)
                        .item(ModItems.CardTicking)
                        .addStr("multiplier")
                        .args(data.multiplier())
                        .build(),
                UtilKeyBuilder.of(Patterns.tooltip)
                        .item(ModItems.CardTicking)
                        .addStr("max")
                        .args(data.maxMultiplier()).build()
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, context, lines, advancedTooltips);
        lines.addAll(this.getTooltipLines(stack));
    }
}


