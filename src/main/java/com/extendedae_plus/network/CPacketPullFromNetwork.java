package com.extendedae_plus.network;

import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.me.helpers.PlayerSource;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftAmountMenu;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.util.WirelessTerminalLocator;
import com.extendedae_plus.util.WirelessTerminalLocator.TerminalInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
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
            ServerLevel level = player.serverLevel();

            if (packet.stack == null) return;
            AEKey what = packet.stack.what();
            if (what.getType() != AEKeyType.items()) return;

            TerminalInfo info = WirelessTerminalLocator.find(player);
            ItemStack terminalStack = info.stack();
            if (terminalStack.isEmpty()) return;

            var uncheckedTerm = terminalStack.getItem();
            if (!(uncheckedTerm instanceof WirelessTerminalItem terminal)) return;
            if (!terminal.hasPower(player, 0.5, terminalStack)) return;

            IGrid grid = terminal.getLinkedGrid(terminalStack, level, null);
            if (grid == null) return;

            if (packet.doPull && setItem(packet, player, terminal, grid, info)) return;

            var craftingService = grid.getCraftingService();
            if (craftingService.isCraftable(what))
                openPlanMenu(packet, player, info);
        });
    }

    private static boolean setItem(CPacketPullFromNetwork packet, ServerPlayer player,
                                   WirelessTerminalItem terminal, IGrid grid, TerminalInfo info) {
        var itemKey = (AEItemKey) packet.stack.what();
        long amount = packet.stack.amount();

        IEnergyService energy = grid.getEnergyService();
        MEStorage storage = grid.getStorageService().getInventory();

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
        terminal.usePower(player, powerUsage, info.stack());

        if (!extractedStack.isEmpty())
            StorageHelper.poweredInsert(energy, storage, itemKey, extractedStack.getCount(), new PlayerSource(player));

        info.commit();
        player.containerMenu.broadcastChanges();

        return true;
    }

    private static void openPlanMenu(CPacketPullFromNetwork packet, ServerPlayer player, TerminalInfo info) {
        var curioSlot = info.curiosIndex();
        var hand = info.hand();
        int slot = info.slotIndex();

        ItemMenuHostLocator locator = null;
        if (curioSlot >= 0) locator = MenuLocators.forCurioSlot(curioSlot);
        else if (hand != null) locator = MenuLocators.forHand(player, hand);
        else if (slot >= 0) locator = MenuLocators.forInventorySlot(slot);

        if (locator != null)
            CraftAmountMenu.open(player, locator, packet.stack.what(), (int) packet.stack.amount());
    }
}
