package com.extendedae_plus.common.registry.item.priorityTool;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.helpers.IPriorityHost;
import appeng.items.AEBaseItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.extendedae_plus.common.registry.menu.host.HostPriorityTool;
import com.extendedae_plus.util.keyBuilder.Patterns;
import com.extendedae_plus.util.keyBuilder.UtilKeyBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemPriorityTool extends AEBaseItem implements IMenuItem {
    public ItemPriorityTool() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        var level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.sidedSuccess(true);

        var data = stack.get(ModDataComponents.Priority);
        if (data == null) {
            MenuOpener.open(ModMenuTypes.PriorityTool.get(), player,
                    MenuLocators.forHand(player, context.getHand()));
            return InteractionResult.CONSUME;
        }

        var blockEntity = level.getBlockEntity(context.getClickedPos());
        if (blockEntity == null) {
            MenuOpener.open(ModMenuTypes.PriorityTool.get(), player,
                    MenuLocators.forHand(player, context.getHand()));
            return InteractionResult.CONSUME;
        }

        IPriorityHost priorityHost = switch (blockEntity) {
            case IPriorityHost host -> host;
            case CableBusBlockEntity cableBusBlockEntity -> {
                var selectedPart = cableBusBlockEntity.selectPartWorld(context.getClickLocation());
                if (selectedPart != null
                        && selectedPart.part instanceof IPriorityHost host) yield host;
                else yield null;
            }
            default -> null;
        };
        if (priorityHost == null) {
            MenuOpener.open(ModMenuTypes.PriorityTool.get(), player,
                    MenuLocators.forHand(player, context.getHand()));
            return InteractionResult.CONSUME;
        }

        priorityHost.setPriority(data.priority());
        stack.set(ModDataComponents.Priority, data.apply());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var item = player.getItemInHand(usedHand);

        if (level.isClientSide())
            return InteractionResultHolder.sidedSuccess(item, true);

        if (!MenuOpener.open(ModMenuTypes.PriorityTool.get(), player, MenuLocators.forHand(player, usedHand)))
            return InteractionResultHolder.fail(item);

        return InteractionResultHolder.consume(item);
    }

    @Override
    public ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator, @Nullable BlockHitResult hitResult) {
        return new HostPriorityTool(this, player, locator);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(UtilKeyBuilder.of(Patterns.tooltip)
                .item(ModItems.PriorityTool)
                .build());
    }
}
