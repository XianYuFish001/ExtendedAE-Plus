package com.extendedae_plus.common.registry.menu;

import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEItems;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.registry.part.ticker.EnergyExtractor;
import com.extendedae_plus.common.registry.part.ticker.PartTicker;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import lombok.Setter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.ItemLike;

public class MenuTicker extends UpgradeableMenu<PartTicker> {
    @GuiSync(701)
    public PartTicker.StateTicker stateTicker = PartTicker.StateTicker.ENABLED;
    @Setter
    @GuiSync(702)
    public boolean stateEnergy = true;
    @Setter
    @GuiSync(703)
    public double costMultiplier = 1D;
    @Setter
    @GuiSync(704)
    public double energyCost = 1D;
    public long speedMultiplier = 1L;
    public double remainingRatio = 1D;
    public ItemLike targetBlock = null;

    @Setter
    private Runnable refreshAction = () -> {};

    public MenuTicker(int containerID, Inventory invPlayer, PartTicker host) {
        super(ModMenuTypes.ticker.get(), containerID, invPlayer, host);
        host.setLogic(this);
    }

    public PartTicker.StateTicker getTickerState() {
        return this.stateTicker;
    }

    public void recalculateCardsEffects() {
        this.speedMultiplier = EnergyExtractor.calculateMultiplier(this.getUpgrades());
        this.remainingRatio = EnergyExtractor.calculateEnergyRemainingRatio(
                this.getUpgrades().getInstalledUpgrades(AEItems.ENERGY_CARD));
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager configManager) {
        this.setRedStoneMode(configManager.getSetting(ModSettings.modeRedstoneOptional));
        this.stateTicker = this.getHost().getConfigManager().getSetting(ModSettings.stateTicker);
    }

    @Override
    public void onSlotChange(Slot s) {
        super.onSlotChange(s);
        this.recalculateCardsEffects();
        this.refreshAction.run();
    }

    @Override
    public void onServerDataSync(ShortSet updatedFields) {
        super.onServerDataSync(updatedFields);
        this.recalculateCardsEffects();
        this.refreshAction.run();
    }

    public void updateTargetBlock(ItemLike targetBlock) {
        this.targetBlock = targetBlock;
    }
}
