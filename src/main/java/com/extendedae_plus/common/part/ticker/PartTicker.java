package com.extendedae_plus.common.part.ticker;

import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.IConfigManagerBuilder;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.automation.UpgradeablePart;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.menu.MenuTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.phys.Vec3;

public class PartTicker extends UpgradeablePart {
    public static final ResourceLocation MODEL_BASE =
            ExtendedAEPlus.getLocation("part/ticker_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            ExtendedAEPlus.getLocation("part/ticker_off"));
    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            ExtendedAEPlus.getLocation("part/ticker_on"));
    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            ExtendedAEPlus.getLocation("part/ticker_formed"));

    private MenuTicker logic = null;

    private long speedMultiplier = 1L;
    private double costMultiplier = 1D;
    private double energyCost = 1D;

    public PartTicker(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setIdlePowerUsage(1)
                .addService(IGridTickable.class, new TickerTicker());
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(ModSettings.STATE_TICKER, StateTicker.ENABLED);
        builder.registerSetting(ModSettings.OPTIONAL_REDSTONE_MODE, RedstoneMode.IGNORE);
    }

    private void prepareTick(BlockEntity blockEntity) {
        if (this.speedMultiplier <= 1) return;
        if (!this.getConfigManager().getSetting(ModSettings.STATE_TICKER).equals(StateTicker.ENABLED)) return;
        if (this.getGridNode() == null) return;

        if (switch (this.getConfigManager().getSetting(ModSettings.OPTIONAL_REDSTONE_MODE)) {
            case LOW_SIGNAL -> this.getHost().hasRedstone();
            case HIGH_SIGNAL -> !this.getHost().hasRedstone();
            default -> false;
        }) return;

        var ticker = blockEntity.getBlockState().getTicker(getLevel(), blockEntity.getType());
        if (ticker == null) return;

        boolean extracted = EnergyExtractor.calculateAndExtractEnergy(this, this.speedMultiplier, this.costMultiplier);
        if (this.logic != null)
            this.logic.setEnergyState(extracted);
        if (!extracted) return;

        this.applyTick(blockEntity, ticker);
    }

    @SuppressWarnings("unchecked")
    private <TBlockEntity extends BlockEntity> void
    applyTick(TBlockEntity blockEntity, BlockEntityTicker<?> ticker) {
        var level = blockEntity.getLevel();
        if (level == null) return;
        for (long i = 0; i < this.speedMultiplier; i++)
            ((BlockEntityTicker<TBlockEntity>) ticker)
                    .tick(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.recalculateAll();
    }

    @Override
    public void upgradesChanged() {
        this.recalculateAll();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (this.getSide() == null) return;
        if (!pos.relative(getSide()).equals(neighbor)) return;

        var targetBlockState = level.getBlockState(neighbor);

        if (targetBlockState.isAir()) {
            this.costMultiplier = 1D;
            this.getConfigManager().putSetting(ModSettings.STATE_TICKER, StateTicker.DISABLED);
        } else {
            this.costMultiplier = ParserTickerConfig.getBlockExternalMultiplier(targetBlockState);
            if (ParserTickerConfig.isBlockBlacklisted(targetBlockState))
                this.getConfigManager().putSetting(ModSettings.STATE_TICKER, StateTicker.BLACKLISTED);
        }
        this.recalculateEnergyCost();
        if (this.logic != null) {
            this.logic.setCostMultiplier(this.costMultiplier);
            this.logic.updateTargetBlock(targetBlockState.getBlock());
        }
    }

    @Override
    public RedstoneMode getRSMode() {
        return this.getConfigManager().getSetting(ModSettings.OPTIONAL_REDSTONE_MODE);
    }

    private void recalculateAll() {
        this.speedMultiplier = EnergyExtractor.calculateMultiplier(this.getUpgrades());
        this.recalculateEnergyCost();
        if (this.logic != null)
            this.logic.setEnergyCost(this.energyCost);
    }

    private void recalculateEnergyCost() {
        this.energyCost = EnergyExtractor.calculateEnergyCost(this.getUpgrades(), this.speedMultiplier, this.costMultiplier);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (player.level().isClientSide()) return true;
        return MenuOpener.open(ModMenuTypes.TICKER.get(), player, MenuLocators.forPart(this));
    }

    public void setLogic(MenuTicker logic) {
        this.logic = logic;
        this.logic.setEnergyCost(this.energyCost);
        this.logic.setCostMultiplier(this.costMultiplier);
        this.logic.recalculateCardsEffects();

        var face = this.getSide();
        if (face == null) return;
        this.logic.updateTargetBlock(
                this.getLevel().getBlockState(
                        this.getBlockEntity().getBlockPos().relative(face)
                ).getBlock()
        );
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) return MODELS_HAS_CHANNEL;
        else if (this.isPowered()) return MODELS_ON;
        else return MODELS_OFF;
    }

    @Override
    protected int getUpgradeSlots() {
        return 8;
    }

    @Override
    public void getBoxes(IPartCollisionHelper collisionHelper) {
        collisionHelper.addBox(2, 2, 14, 14, 14, 16);
        collisionHelper.addBox(5, 5, 12, 11, 11, 14);
    }

    private class TickerTicker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(1, 1, false);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (getSide() == null)
                return TickRateModulation.SLEEP;
            if (!getConfigManager().getSetting(ModSettings.STATE_TICKER).equals(StateTicker.ENABLED))
                return TickRateModulation.SLEEP;

            var targetBlockEntity = getLevel().getBlockEntity(getBlockEntity().getBlockPos().relative(getSide()));
            if (targetBlockEntity == null || !isActive())
                return TickRateModulation.SLOWER;

            prepareTick(targetBlockEntity);
            return TickRateModulation.IDLE;
        }
    }

    public enum StateTicker {
        ENABLED, DISABLED, BLACKLISTED
    }
}
