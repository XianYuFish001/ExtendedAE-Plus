package com.extendedae_plus.common.registry.item.upgradeCard;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.upgrades.Upgrades;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.items.materials.UpgradeCardItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.extendedae_plus.common.registry.dataComponent.DataChannelCard;
import com.extendedae_plus.common.registry.menu.host.linkLabel.HostCardChannel;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 频道卡：存储频道号、所有者UUID和团队信息
 * - 右键空气：增加频道号
 * - 潜行右键空气：减少频道号
 * - 潜行左键空气：写入/清除玩家UUID和团队信息（通过网络包处理）
 * - 潜行左键收发器：将频道卡的所有者信息写入收发器
 * 继承 AE2 的 UpgradeCardItem 以复用升级卡判定与提示框架。
 */
public class CardChannel extends UpgradeCardItem implements IMenuItem {
    public CardChannel() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);

        // 显示频道
        var label = DataChannelCard.getLabel(stack);
        lines.add(UtilKeyBuilder.of(UtilKeyBuilder.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .addStr("label")
                .addStr(label.isEmpty(), "unset")
                .args(label.getDisplayValue(), label.description().getString())
                .build());

        // 显示所有者信息
        UUID ownerUUID = DataChannelCard.getOwnerUUID(stack);
        String teamName = DataChannelCard.getOwnerName(stack);

        lines.add(UtilKeyBuilder.of(UtilKeyBuilder.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .addStr(!teamName.isEmpty(), "name")
                .addStr(teamName.isEmpty() && ownerUUID != null, "id")
                .args(teamName, ownerUUID != null ? ownerUUID.toString().substring(0, 8) : null)
                .build());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        MenuOpener.open(ModMenuTypes.labelLink.get(), player, MenuLocators.forHand(player, hand));
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());

        ItemLike currentBlock;
        if (blockEntity instanceof CableBusBlockEntity cableBus) {
            currentBlock = cableBus.selectPartWorld(context.getClickLocation()).part.getPartItem();
        } else {
            currentBlock = context.getLevel()
                    .getBlockState(context.getClickedPos())
                    .getBlock()
                    .asItem();
        }

        boolean available = Upgrades.getMaxInstallable(ModItems.CHANNEL_CARD, currentBlock) > 0;
        if (available) return InteractionResult.FAIL;
        else return super.useOn(context);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        boolean emptyOwner = DataChannelCard.getOwnerUUID(stack) == null;
        var player = context.getPlayer();
        if (!emptyOwner || context.getLevel().isClientSide()|| player == null || !player.isShiftKeyDown())
            return super.onItemUseFirst(stack, context);

        DataChannelCard.setOwner(stack, player.getUUID(), player.getName().getString());
        var result = super.onItemUseFirst(stack, context);
        DataChannelCard.clearOwner(player.getItemInHand(context.getHand()));
        return result;
    }

    @Override
    public @Nullable ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new HostCardChannel(this, player, locator);
    }
}
