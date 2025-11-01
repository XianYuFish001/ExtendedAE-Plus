package com.extendedae_plus.util;

import appeng.api.networking.IGrid;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class WirelessTerminalLocator {
    public static Optional<TerminalInfo> locate(Player player) {
        var inventory = player.getInventory();

        if (ModList.get().isLoaded("curios")) {
            try {
                AtomicReference<TerminalInfo> info = new AtomicReference<>();
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    var resultOptional = handler.findFirstCurio(
                            itemStack -> itemStack.getItem() instanceof WirelessTerminalItem);
                    if (resultOptional.isEmpty()) return;
                    var result = resultOptional.get();

                    info.set(new TerminalInfo(result.stack(), (WirelessTerminalItem) result.stack().getItem(),
                            new LocatedSlotContext(player, false, -1, result.slotContext().index())));
                });
                if (info.get() != null) return Optional.of(info.get());
            } catch (Throwable ignore) {
            }
        }

        if (inventory.offhand.getFirst().getItem() instanceof WirelessTerminalItem terminal)
            return Optional.of(new TerminalInfo(inventory.offhand.getFirst(), terminal,
                    new LocatedSlotContext(player, true, -1, -1)));

        for (int invIndex = 0; invIndex < inventory.items.size(); invIndex++) {
            var item = inventory.items.get(invIndex);
            if (!(item.getItem() instanceof WirelessTerminalItem terminal))
                continue;

            return Optional.of(new TerminalInfo(item, terminal,
                    new LocatedSlotContext(player, false, invIndex, -1)));
        }

        return Optional.empty();
    }

    public record TerminalInfo(ItemStack terminalStack, WirelessTerminalItem terminal,
                               LocatedSlotContext context) {
        public boolean isWTLibTerminal() {
            if (ModList.get().isLoaded("ae2wtlib"))
                return terminalStack.getItem() instanceof ItemWT;
            else return false;
        }

        public Optional<ItemMenuHostLocator> getMenuLocator() {
            var curioSlot = context.curioIndex;
            var slot = context.invIndex;

            ItemMenuHostLocator locator = null;
            if (curioSlot >= 0) locator = MenuLocators.forCurioSlot(curioSlot);
            else if (context.offhand) locator = MenuLocators.forHand(context.player, InteractionHand.OFF_HAND);
            else if (slot >= 0) locator = MenuLocators.forInventorySlot(slot);

            return Optional.ofNullable(locator);
        }

        public Optional<IGrid> grid() {
            var menuLocator = getMenuLocator();
            if (isWTLibTerminal() && menuLocator.isPresent()) {
                var node = ((ItemWT) terminal)
                        .getMenuHost(context.player, menuLocator.get(), null)
                        .getActionableNode();
                if (node == null) return Optional.empty();
                return Optional.ofNullable(node.getGrid());
            }

            return Optional.ofNullable(
                    terminal.getLinkedGrid(terminalStack, context.player.level(), null));
        }
    }

    public record LocatedSlotContext(Player player, boolean offhand, int invIndex, int curioIndex) {
    }
}
