package com.extendedae_plus.common.item;

import appeng.api.networking.IInWorldGridNodeHost;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.menu.MenuProviderController;
import com.extendedae_plus.util.UtilGetKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;

public class ItemProviderController extends Item implements MenuProvider {
    public ItemProviderController() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.sidedSuccess(true);

        var item = context.getItemInHand();
        if (!(item.getItem() instanceof MenuProvider provider)) return super.useOn(context);

        var player = context.getPlayer();
        if (player == null) return super.useOn(context);

        var rawBlockEntity = level.getBlockEntity(context.getClickedPos());
        if (rawBlockEntity instanceof IInWorldGridNodeHost) {
            player.openMenu(provider, buf -> {
                buf.writeBlockPos(context.getClickedPos());
                buf.writeUtf(context.getClickedFace().getName());
            });
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public Component getDisplayName() {
        return new UtilGetKey(UtilGetKey.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .build();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MenuProviderController(i, inventory, null);
    }
}
