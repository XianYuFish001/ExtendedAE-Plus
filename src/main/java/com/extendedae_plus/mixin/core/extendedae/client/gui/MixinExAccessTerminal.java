package com.extendedae_plus.mixin.core.extendedae.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.AEBaseMenu;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.client.render.widgets.button.EAEPCycleButton;
import com.extendedae_plus.mixin.core.extendedae.accessor.AccessorExAccessScreenRows;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderSelectionApplier;
import com.extendedae_plus.network.CPacketUploadInventoryPattern;
import com.glodblock.github.extendedae.client.button.HighlightButton;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal;
import com.glodblock.github.extendedae.container.ContainerExPatternTerminal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;

@Mixin(GuiExPatternTerminal.class)
public abstract class MixinExAccessTerminal extends AEBaseScreen<AEBaseMenu>
        implements HelperProviderSelectionApplier {
    @Shadow
    @Final
    private ArrayList<Object> rows;
    @Shadow
    @Final
    private Scrollbar scrollbar;

    @Shadow
    protected abstract void resetScrollbar();
    @Shadow
    protected abstract void refreshList();

    @Shadow
    @Final
    private HashMap<Integer, HighlightButton> highlightBtns;
    @Unique
    @Nullable
    private Long eaep$selectedProvider = null;
    @Unique
    private boolean eaep$rowSlotsVisible = true;

    public MixinExAccessTerminal(AEBaseMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(ContainerExPatternTerminal menu,
                        Inventory playerInventory,
                        Component title,
                        ScreenStyle style,
                        CallbackInfo ci) {
        var buttonRowSlotVisible = new EAEPCycleButton.Builder()
                .addPart(EAEPActionItems.ROW_SLOTS_VISIBLE, this::eaep$toggleRowSlotsVisible)
                .addPart(EAEPActionItems.ROW_SLOTS_INVISIBLE, this::eaep$toggleRowSlotsVisible)
                .build();
        buttonRowSlotVisible.setStateIndex(
                EAEPConfig.PATTERN_TERMINAL_SHOW_SLOTS_DEFAULT.getAsBoolean() ? 0 : 1);
        this.addToLeftToolbar(buttonRowSlotVisible);
    }

    @Inject(method = "refreshList", at = @At("TAIL"))
    private void onRefresh(CallbackInfo ci) {
        if (this.eaep$rowSlotsVisible) return;

        var indexRow = 0;
        for (int index = 0; index < this.rows.size(); index++) {
            var row = this.rows.get(index);
            if (row instanceof AccessorExAccessScreenRows) continue;
            this.rows.set(indexRow, row);
            indexRow++;

            var buttonHighlight = this.highlightBtns.get(index + 1);
            this.highlightBtns.remove(index + 1);
            if (buttonHighlight == null) continue;
            this.highlightBtns.putIfAbsent(indexRow - 1, buttonHighlight);
        }

        while (this.rows.size() > indexRow)
            this.rows.removeLast();

        this.resetScrollbar();
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(Slot slot,
                             int slotIdx,
                             int mouseButton,
                             ClickType clickType,
                             CallbackInfo ci) {
        if (!clickType.equals(ClickType.QUICK_MOVE) || !this.menu.isPlayerSideSlot(slot)) return;

        if (this.eaep$selectedProvider == null) {
            var levelScroll = this.scrollbar.getCurrentScroll();

            var row = this.rows.get(levelScroll);
            if (!(row instanceof AccessorExAccessScreenRows))
                row = this.rows.get(levelScroll + 1);
            if (!(row instanceof AccessorExAccessScreenRows accessor))
                return;

            this.eaep$selectedProvider = accessor.getContainer().getServerId();
        }

        PacketDistributor.sendToServer(new CPacketUploadInventoryPattern(slotIdx, this.eaep$selectedProvider));

        ci.cancel();
    }

    @Unique
    private void eaep$toggleRowSlotsVisible(EAEPActionItems action) {
        this.eaep$rowSlotsVisible = action.equals(EAEPActionItems.ROW_SLOTS_VISIBLE);
        this.refreshList();
    }

    @Override
    public void eaep$selectProvider(long serverID) {
        this.eaep$selectedProvider = serverID;
    }
}
