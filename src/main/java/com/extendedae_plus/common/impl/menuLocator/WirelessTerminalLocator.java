package com.extendedae_plus.common.impl.menuLocator;

import appeng.api.networking.IGrid;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import com.extendedae_plus.integration.ContextModLoaded;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class WirelessTerminalLocator {
    public static Optional<TerminalInfo> locate(Player player) {
        var inventory = player.getInventory();

        if (ContextModLoaded.curios.isLoaded()) {
            try {
                AtomicReference<TerminalInfo> info = new AtomicReference<>();
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    var resultOptional = handler.findFirstCurio(
                            itemStack -> itemStack.getItem() instanceof WirelessTerminalItem);
                    if (resultOptional.isEmpty()) return;
                    var result = resultOptional.get();

                    info.set(new TerminalInfo(result.stack(), (WirelessTerminalItem) result.stack().getItem(),
                            new LocatedSlotContext(player, false, -1,
                                    result.slotContext().identifier(), result.slotContext().index())));
                });
                if (info.get() != null) return Optional.of(info.get());
            } catch (Throwable ignore) {
            }
        }

        if (inventory.offhand.getFirst().getItem() instanceof WirelessTerminalItem terminal)
            return Optional.of(new TerminalInfo(inventory.offhand.getFirst(), terminal,
                    new LocatedSlotContext(player, true, -1, "", -1)));

        for (int invIndex = 0; invIndex < inventory.items.size(); invIndex++) {
            var item = inventory.items.get(invIndex);
            if (!(item.getItem() instanceof WirelessTerminalItem terminal))
                continue;

            return Optional.of(new TerminalInfo(item, terminal,
                    new LocatedSlotContext(player, false, invIndex, "", -1)));
        }

        return Optional.empty();
    }

    public record TerminalInfo(ItemStack terminalStack, WirelessTerminalItem terminal,
                               LocatedSlotContext context) {
        public boolean isWTLibTerminal() {
            if (ContextModLoaded.ae2wtlib.isLoaded())
                return terminalStack.getItem() instanceof ItemWT;
            else return false;
        }

        public Optional<ItemMenuHostLocator> getMenuLocator() {
            var curioType = context.curioType;
            var curioIndex = context.curioIndex;
            var slot = context.invIndex;

            ItemMenuHostLocator locator = null;
            if (curioIndex >= 0) locator = new CuriosItemLocator(curioType, curioIndex, null);
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

    public record LocatedSlotContext(Player player, boolean offhand, int invIndex, String curioType, int curioIndex) {
    }
}
