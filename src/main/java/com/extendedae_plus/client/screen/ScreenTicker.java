package com.extendedae_plus.client.screen;

import appeng.api.config.RedstoneMode;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.util.Platform;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.menu.MenuTicker;
import com.extendedae_plus.common.part.ticker.PartTicker;
import com.extendedae_plus.util.GuiUtil;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ScreenTicker extends UpgradeableScreen<MenuTicker> {
    private final SettingToggleButton<PartTicker.StateTicker> buttonStateSwitcher;
    private final SettingToggleButton<RedstoneMode> buttonRSMode;

    public ScreenTicker(MenuTicker menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        menu.setRefreshAction(this::textTooltip);

        this.buttonStateSwitcher = new SettingToggleButton<>(
                ModSettings.STATE_TICKER, PartTicker.StateTicker.ENABLED,
                (button, reversed) -> {
                    if (PartTicker.StateTicker.BLACKLISTED.equals(this.getMenu().getTickerState())) return;
                    PacketDistributor.sendToServer(new ConfigButtonPacket(button.getSetting(), reversed));
                });
        this.buttonRSMode = new ServerSettingToggleButton<>(ModSettings.OPTIONAL_REDSTONE_MODE, RedstoneMode.IGNORE);

        this.addToLeftToolbar(this.buttonStateSwitcher);
        this.addToLeftToolbar(this.buttonRSMode);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        GuiUtil.renderScalableFakeItem(
                guiGraphics,
                new ItemStack(this.menu.targetBlock),
                this.leftPos + 92,
                this.topPos + 28,
                4
        );
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.buttonStateSwitcher.set(this.menu.getTickerState());
        this.buttonRSMode.set(this.menu.getRedStoneMode());

        this.textTooltip();
    }

    private void textTooltip() {
        long speedMultiplier;
        double energyCost;
        double remainingRatio;
        double costMultiplier;
        boolean blacklisted = this.menu.getTickerState().equals(PartTicker.StateTicker.BLACKLISTED);
        if (blacklisted) {
            speedMultiplier = 1L;
            energyCost = 0D;
            remainingRatio = 1D;
            costMultiplier = 1D;
        } else {
            speedMultiplier = this.menu.speedMultiplier;
            energyCost = this.menu.energyCost;
            remainingRatio = this.menu.remainingRatio;
            costMultiplier = this.menu.costMultiplier;
        }

        var builder = UtilKeyBuilder.of(UtilKeyBuilder.screen)
                .item(ModItems.PART_TICKER)
                .newHashMap();

        builder.addStr(switch (this.menu.getTickerState()) {
            case BLACKLISTED -> "blacklisted";
            case DISABLED -> "disabled";
            case ENABLED -> {
                if (this.menu.energySufficient) yield "enabled";
                else yield "needs_energy";
            }
        }).buildIntoPlain("state");
        builder.args(speedMultiplier)
                .buildInto("speed_multiplier");
        builder.args(Platform.formatPower(energyCost, false))
                .buildInto("energy_cost");
        builder.args(String.format("%.2f%%", remainingRatio))
                .buildInto("power_ratio");
        builder.args(String.format("%.2fx", costMultiplier))
                .buildInto("cost_multiplier");

        builder.getMap().forEach(this::setTextContent);
    }
}
