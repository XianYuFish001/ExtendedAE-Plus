package com.extendedae_plus.common.item.upgradeCard;

import appeng.api.upgrades.Upgrades;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.items.materials.UpgradeCardItem;
import com.extendedae_plus.common.dataComponent.DataChannelCard;
import com.extendedae_plus.common.init.ModItems;
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
public class ChannelCardItem extends UpgradeCardItem {
    public ChannelCardItem() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);

        // 显示频道
        long ch = DataChannelCard.getFrequency(stack);
        lines.add(UtilKeyBuilder.of(UtilKeyBuilder.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .addStr("frequency")
                .addStr(ch == 0, "unset")
                .args(ch)
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

        long ch = DataChannelCard.getFrequency(stack);
        long next = Math.max(0, ch + (player.isShiftKeyDown() ? -1 : 1));

        if (next != ch) {
            DataChannelCard.setFrequency(stack, next);
            player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.tooltip)
                            .item(ModItems.CHANNEL_CARD)
                            .addStr("frequency")
                            .addStr("set_to")
                            .addStr(next == 0, "none")
                            .args(next)
                            .build(),
                    true);
        }

        if (DataChannelCard.getOwnerUUID(stack) == null) {
            DataChannelCard.setOwner(stack, player.getUUID(), player.getName().getString());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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
}
