package com.extendedae_plus.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton;
import com.extendedae_plus.common.registry.menu.MenuPriorityTool;
import com.extendedae_plus.network.CPacketPriorityToolOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ScreenPriorityTool extends AEBaseScreen<MenuPriorityTool> {
    private final NumberEntryWidget priority;
    private final EAEPServerCycleButton buttonCycleMode;

    public ScreenPriorityTool(MenuPriorityTool menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.priority = widgets.addNumberEntryWidget("priority", NumberEntryType.UNITLESS);
        this.priority.setTextFieldStyle(style.getWidget("priorityInput"));
        this.priority.setMinValue(Integer.MIN_VALUE);
        this.priority.setLongValue(menu.getData().priority());
        this.priority.setOnChange(this::savePriority);
        this.priority.setOnConfirm(() -> {
            savePriority();
            this.onClose();
        });

        this.buttonCycleMode = new EAEPServerCycleButton.Builder()
                .setTask(new CPacketPriorityToolOperation(null, true))
                .addPart(EAEPActionItems.PRIORITY_KEEP)
                .addPart(EAEPActionItems.PRIORITY_INCREMENT)
                .addPart(EAEPActionItems.PRIORITY_DECREMENT)
                .setSyncer(() -> menu.getData().modeTool())
                .build();
        this.addToLeftToolbar(this.buttonCycleMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.buttonCycleMode.updateState();
    }

    private void savePriority() {
        PacketDistributor.sendToServer(new CPacketPriorityToolOperation(
                this.priority.getIntValue().orElse(0), false));
    }
}
