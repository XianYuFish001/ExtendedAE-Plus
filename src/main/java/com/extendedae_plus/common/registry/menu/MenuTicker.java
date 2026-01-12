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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.ItemLike;

public class MenuTicker extends UpgradeableMenu<PartTicker> {
    @GuiSync(701)
    public PartTicker.StateTicker stateTicker = PartTicker.StateTicker.ENABLED;
    @GuiSync(702)
    public boolean energySufficient = true;
    @GuiSync(703)
    public double costMultiplier = 1D;
    @GuiSync(704)
    public double energyCost = 1D;
    public long speedMultiplier = 1L;
    public double remainingRatio = 1D;
    public ItemLike targetBlock = null;

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
        this.setRedStoneMode(configManager.getSetting(ModSettings.OPTIONAL_REDSTONE_MODE));
        this.stateTicker = this.getHost().getConfigManager().getSetting(ModSettings.STATE_TICKER);
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

    public void setRefreshAction(Runnable refreshAction) {
        this.refreshAction = refreshAction;
    }

    public void setEnergyState(boolean energySufficient) {
        this.energySufficient = energySufficient;
    }

    public void setCostMultiplier(double costMultiplier) {
        this.costMultiplier = costMultiplier;
    }

    public void setEnergyCost(double energyCost) {
        this.energyCost = energyCost;
    }
}
