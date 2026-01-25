package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.util.inv.AppEngInternalInventory;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.common.init.ModItems;
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class BlockEntityAdvancedPattern extends TileAssemblerMatrixPattern {
    private final AppEngInternalInventory patternInventory =
            new AppEngInternalInventory(this, getInvSize(), 1);
    private final List<IPatternDetails> patterns = new ArrayList<>();

    public BlockEntityAdvancedPattern(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
        this.patternInventory.setFilter(new Filter(this::getLevel));
    }

    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.patternInventory.writeToNBT(data, "pattern", registries);
    }

    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.patternInventory.readFromNBT(data, "pattern", registries);
    }

    public AppEngInternalInventory getPatternInventory() {
        return this.patternInventory;
    }

    public AppEngInternalInventory getExposedInventory() {
        return this.patternInventory;
    }

    public void updatePatterns() {
        this.patterns.clear();

        for(ItemStack stack : this.patternInventory) {
            IPatternDetails details = PatternDetailsHelper.decodePattern(stack, this.getLevel());
            if (details != null) {
                this.patterns.add(details);
            }
        }

        ICraftingProvider.requestUpdate(this.getMainNode());
    }

    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for(ItemStack pattern : this.patternInventory) drops.add(pattern);
    }

    public void clearContent() {
        super.clearContent();
        this.patternInventory.clear();
    }

    public void add(ClusterAssemblerMatrix cluster) {
        cluster.addPattern(this);
    }

    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return this.isFormed()
                && this.getMainNode().isActive()
                && this.patterns.contains(patternDetails)
                && this.cluster.pushCraftingJob(patternDetails, inputHolder);
    }

    public InternalInventory getTerminalPatternInventory() {
        return this.patternInventory;
    }

    public PatternContainerGroup getTerminalGroup() {
        AEItemKey icon = AEItemKey.of(ModItems.CORE_ADVANCED_PATTERN);
        Component name = this.hasCustomName() ? this.getCustomName() : icon.getDisplayName();
        return new PatternContainerGroup(icon, name, List.of(Component.translatable("gui.extendedae.assembler_matrix.pattern")));
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.CORE_ADVANCED_PATTERN.get();
    }

    public static int getInvSize() {
        return INV_SIZE * EAEPConfig.corePatternSlotMultiplier.getAsInt();
    }
}
