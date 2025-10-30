package com.extendedae_plus.util;

import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.function.Consumer;

/**
 * 定位玩家身上的无线终端：
 * - 原版槽位：主手、副手、盔甲、背包
 * - 若加载了 Curios：遍历所有饰品槽
 * 返回一个可写回的结果，以便能量消耗等 NBT 变更能持久化。
 */
public final class WirelessTerminalLocator {
    /**
     * @param slotIndex    在玩家 Inventory 中的槽位索引（0..size-1）。若未知则为 -1。
     * @param hand         若终端在玩家手上，则记录手别；否则为 null。
     * @param curiosSlotId 若终端位于 Curios，则记录其槽位组 ID 与组内索引；否则 slotId 为 null，index 为 -1。
     */
    public record TerminalInfo(ItemStack stack, Consumer<ItemStack> setter, int slotIndex, InteractionHand hand,
                               String curiosSlotId, int curiosIndex) {
            public TerminalInfo(ItemStack stack, Consumer<ItemStack> setter) {
                this(stack, setter, -1, null, null, -1);
            }

            public TerminalInfo(ItemStack stack, Consumer<ItemStack> setter, int slotIndex) {
                this(stack, setter, slotIndex, null, null, -1);
            }

            public TerminalInfo(ItemStack stack, Consumer<ItemStack> setter, int slotIndex, InteractionHand hand) {
                this(stack, setter, slotIndex, hand, null, -1);
            }

        public void set(ItemStack newStack) {
            this.setter.accept(newStack);
        }

        public void commit() {
            this.setter.accept(this.stack);
        }

        public boolean isEmpty() {
            return this.stack == null || this.stack.isEmpty();
        }

        /**
         * 若返回 -1，说明不是从原版 Inventory 槽位中找到（比如 Curios）。
         */
            @Override
            public int slotIndex() {
                return this.slotIndex;
            }

        /**
         * 若不为 null，说明终端在玩家手上。
         */
            @Override
            public InteractionHand hand() {
                return this.hand;
            }

        /**
         * 若不为 null，说明终端位于 Curios 指定槽位组。
         */
            @Override
            public String curiosSlotId() {
                return this.curiosSlotId;
            }

        /**
         * Curios 组内索引，未知时为 -1。
         */
            @Override
            public int curiosIndex() {
                return this.curiosIndex;
            }
        }

    public static TerminalInfo find(Player player) {
        if (player == null) return new TerminalInfo(ItemStack.EMPTY, s -> {});

        // 1) 先检查主手/副手
        var main = player.getMainHandItem();
        if (!main.isEmpty() && (main.getItem() instanceof WirelessCraftingTerminalItem || main.getItem() instanceof WirelessTerminalItem)) {
            return new TerminalInfo(main, (ns) -> player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ns), -1, net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        var off = player.getOffhandItem();
        if (!off.isEmpty() && (off.getItem() instanceof WirelessCraftingTerminalItem || off.getItem() instanceof WirelessTerminalItem)) {
            return new TerminalInfo(off, (ns) -> player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ns), -1, net.minecraft.world.InteractionHand.OFF_HAND);
        }

        // 2) 原版槽位
        var inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack st = inv.getItem(i);
            if (!st.isEmpty() && (st.getItem() instanceof WirelessCraftingTerminalItem || st.getItem() instanceof WirelessTerminalItem)) {
                final int slot = i;
                return new TerminalInfo(st, (ns) -> inv.setItem(slot, ns), slot);
            }
        }

        // 3) Curios 饰品槽（若已加载）
        if (ModList.get().isLoaded("curios")) {
            try {
                var opt = CuriosApi.getCuriosInventory(player);
                if (opt.isPresent()) {
                    ICuriosItemHandler handler = opt.get();
                    for (var entry : handler.getCurios().entrySet()) {
                        String slotId = entry.getKey();
                        ICurioStacksHandler stacksHandler = entry.getValue();
                        IDynamicStackHandler stacks = stacksHandler.getStacks();
                        int slots = stacks.getSlots();
                        for (int i = 0; i < slots; i++) {
                            ItemStack st = stacks.getStackInSlot(i);
                            if (!st.isEmpty() && (st.getItem() instanceof WirelessCraftingTerminalItem || st.getItem() instanceof WirelessTerminalItem)) {
                                final int slot = i;
                                java.util.function.Consumer<ItemStack> setter = (ns) -> stacks.setStackInSlot(slot, ns);
                                return new TerminalInfo(st, setter, -1, null, slotId, slot);
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
                // 若 Curios API 在运行时不可用或发生异常，则忽略并返回空
            }
        }

        return new TerminalInfo(ItemStack.EMPTY, s -> {}, -1, null);
    }
}
