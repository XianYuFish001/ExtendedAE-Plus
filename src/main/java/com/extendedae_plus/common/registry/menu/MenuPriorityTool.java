package com.extendedae_plus.common.registry.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority;
import com.extendedae_plus.common.registry.menu.host.HostPriorityTool;
import net.minecraft.world.entity.player.Inventory;

public class MenuPriorityTool extends AEBaseMenu {
    private final HostPriorityTool host;

    @GuiSync(0)
    public int priority = 0;
    @GuiSync(1)
    public DataPriority.ModeTool mode = DataPriority.ModeTool.KEEP;

    public MenuPriorityTool(int id, Inventory playerInventory, HostPriorityTool host) {
        super(ModMenuTypes.priorityTool.get(), id, playerInventory, host);
        this.host = host;

        var data = host.getData();
        if (data == null) {
            this.setData(new DataPriority(0, DataPriority.ModeTool.KEEP));
            return;
        }
        this.priority = data.priority();
        this.mode = data.modeTool();
    }

    public DataPriority getData() {
        return new DataPriority(this.priority, this.mode);
    }

    public void setData(DataPriority data) {
        this.host.setData(data);

        var hostData = host.getData();
        this.priority = hostData.priority();
        this.mode = hostData.modeTool();
    }
}
