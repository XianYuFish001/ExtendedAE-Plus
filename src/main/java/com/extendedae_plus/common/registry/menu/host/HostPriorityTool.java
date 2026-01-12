package com.extendedae_plus.common.registry.menu.host;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority;
import com.extendedae_plus.common.registry.item.priorityTool.ItemPriorityTool;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class HostPriorityTool extends ItemMenuHost<ItemPriorityTool> {
    public HostPriorityTool(ItemPriorityTool item, Player player, ItemMenuHostLocator locator) {
        super(item, player, locator);

        var itemStack = this.getItemStack();
        if (!itemStack.has(ModDataComponents.DATA_PRIORITY)) {
            itemStack.set(ModDataComponents.DATA_PRIORITY,
                    new DataPriority(0, DataPriority.ModeTool.KEEP));
        }
    }

    public @Nullable DataPriority getData() {
        return this.getItemStack().get(ModDataComponents.DATA_PRIORITY);
    }

    public void setData(DataPriority data) {
        this.getItemStack().set(ModDataComponents.DATA_PRIORITY, data);
    }
}
