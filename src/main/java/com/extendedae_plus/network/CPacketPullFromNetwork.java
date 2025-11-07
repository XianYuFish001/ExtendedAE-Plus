package com.extendedae_plus.network;

import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.PlayerSource;
import appeng.menu.me.crafting.CraftAmountMenu;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.impl.menuLocator.WirelessTerminalLocator;
import com.extendedae_plus.common.impl.menuLocator.WirelessTerminalLocator.TerminalInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CPacketPullFromNetwork(GenericStack stack, boolean doPull, boolean toInventory) implements CustomPacketPayload {
    public static final Type<CPacketPullFromNetwork> TYPE = new Type<>(
            ExtendedAEPlus.getLocation("pull_from_network"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketPullFromNetwork> STREAM_CODEC = StreamCodec.composite(
            GenericStack.STREAM_CODEC, CPacketPullFromNetwork::stack,
            ByteBufCodecs.BOOL, CPacketPullFromNetwork::doPull,
            ByteBufCodecs.BOOL, CPacketPullFromNetwork::toInventory,
            CPacketPullFromNetwork::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final CPacketPullFromNetwork packet, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            if (packet.stack == null) return;
            AEKey what = packet.stack.what();
            if (what.getType() != AEKeyType.items()) return;

            var infoOptional = WirelessTerminalLocator.locate(player);
            if (infoOptional.isEmpty()) return;
            var info = infoOptional.get();

            var terminalStack = info.terminalStack();
            if (terminalStack.isEmpty()) return;

            var gridOptional = info.grid();
            if (gridOptional.isEmpty()) return;
            var grid = gridOptional.get();

            if (packet.doPull && setItem(packet, player, info)) return;

            var craftingService = grid.getCraftingService();
            if (craftingService.isCraftable(what))
                openPlanMenu(packet, player, info);
        });
    }

    private static boolean setItem(CPacketPullFromNetwork packet, ServerPlayer player, TerminalInfo info) {
        var itemKey = (AEItemKey) packet.stack.what();
        var amount = packet.stack.amount();
        if (info.grid().isEmpty()) return false;

        IEnergyService energy = info.grid().get().getEnergyService();
        MEStorage storage = info.grid().get().getStorageService().getInventory();

        long extracted = StorageHelper.poweredExtraction(energy, storage, itemKey, amount, new PlayerSource(player));
        if (extracted <= 0) return false;

        ItemStack extractedStack = itemKey.toStack((int) extracted);
        Inventory playerInv = player.getInventory();

        if (!packet.toInventory) {
            ItemStack cursorStack = player.containerMenu.getCarried();

            if (cursorStack.isEmpty()) {
                player.containerMenu.setCarried(extractedStack.copyAndClear());
            } else if (ItemStack.isSameItemSameComponents(cursorStack, extractedStack)) {
                int maxStackSize = cursorStack.getMaxStackSize();
                int totalAmount = cursorStack.getCount() + extractedStack.getCount();

                if (totalAmount <= maxStackSize) {
                    cursorStack.setCount(totalAmount);
                    player.containerMenu.setCarried(cursorStack);
                    extractedStack.setCount(0);
                } else {
                    int overflow = totalAmount - maxStackSize;
                    cursorStack.setCount(maxStackSize);
                    player.containerMenu.setCarried(cursorStack);

                    ItemStack overflowStack = extractedStack.copyWithCount(overflow);
                    playerInv.add(overflowStack);
                    extractedStack.setCount(overflowStack.getCount());
                }
            } else playerInv.add(extractedStack);
        } else playerInv.add(extractedStack);

        double powerUsage = Math.max(0.5, (extracted - extractedStack.getCount()) * 0.05);
        info.terminal().usePower(player, powerUsage, info.terminalStack());

        if (!extractedStack.isEmpty())
            StorageHelper.poweredInsert(energy, storage, itemKey, extractedStack.getCount(), new PlayerSource(player));

        player.containerMenu.broadcastChanges();
        return true;
    }

    private static void openPlanMenu(CPacketPullFromNetwork packet, ServerPlayer player, TerminalInfo info) {
        var locator = info.getMenuLocator();
        locator.ifPresent(menuLocator ->
                CraftAmountMenu.open(player, menuLocator, packet.stack.what(), (int) packet.stack.amount()));
    }
}
