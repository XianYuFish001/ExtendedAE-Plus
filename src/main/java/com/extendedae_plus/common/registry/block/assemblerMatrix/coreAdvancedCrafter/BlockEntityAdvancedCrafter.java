package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.mixin.impl.bridge.HelperAssemblerMatrixModifier;
import com.glodblock.github.extendedae.common.me.CraftingMatrixThread;
import com.glodblock.github.extendedae.common.me.CraftingThread;
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.IntStream;

public class BlockEntityAdvancedCrafter extends TileAssemblerMatrixCrafter {
    private final CraftingThread[] threads;
    private final InternalInventory internalInv;
    private short states = 0;
    private int currentThread = EAEPConfig.baseCoreCrafterThreads.getAsInt();

    public BlockEntityAdvancedCrafter(BlockPos pos, BlockState blockState) {
        super(pos, blockState);

        this.threads = new CraftingThread[getMaxThread()];
        var inventories = new InternalInventory[getMaxThread()];
        IntStream.range(0, getMaxThread()).forEach(thread -> {
            this.threads[thread] = new CraftingMatrixThread(
                    this, this::getSource, signal -> this.changeState(thread, signal));
            inventories[thread] = this.threads[thread].getInternalInventory();
        });
        this.internalInv = new CombinedInternalInventory(inventories);
    }

    private IActionSource getSource() {
        return this.cluster.getSrc();
    }

    private void changeState(int index, boolean state) {
        boolean oldState = this.states > 0;
        if (state) {
            this.states = (short)(this.states | 1 << index);
        } else {
            this.states = (short)(this.states & ~(1 << index));
        }

        if (state) {
            if (!oldState) {
                this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
            }
        } else if (oldState && this.states <= 0) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().sleepDevice(node));
        }
    }

    public int usedThread() {
        int used = 0;
        for (int threadIndex = 0; threadIndex < this.currentThread; threadIndex++) {
            var thread = this.threads[threadIndex];
            if (thread.getCurrentPattern() != null || !thread.getInternalInventory().isEmpty())
                used++;
        }

        return used;
    }

    public boolean pushJob(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        for (int thread = 0; thread < this.currentThread; thread++) {
            if (this.threads[thread].acceptJob(patternDetails, inputHolder, Direction.DOWN)) {
                ((HelperAssemblerMatrixModifier) this.cluster).eaep$updateCrafter(this);
                return true;
            }
        }

        return false;
    }

    public void stop() {
        for (CraftingThread thread : this.threads) thread.stop();
    }

    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);

        IntStream.range(0, getMaxThread()).forEach(thread ->
                data.put("#thread{" + thread + "}", this.threads[thread].writeNBT(registries)));

        var inventory = new CompoundTag();
        IntStream.range(0, this.internalInv.size()).forEach(invIndex ->
                inventory.put("item{" + invIndex + "}",
                        this.internalInv.getStackInSlot(invIndex).saveOptional(registries)));
        data.put("inv", inventory);
    }

    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);

        IntStream.range(0, getMaxThread()).forEach(thread -> {
            var dataKey = "#thread{" + thread + "}";
            if (!data.contains(dataKey)) return;
            this.threads[thread].readNBT(data.getCompound(dataKey), registries);
        });

        var inventory = data.getCompound("inv");
        IntStream.range(0, this.internalInv.size()).forEach(invIndex -> {
            var item = inventory.getCompound("item{" + invIndex + "}");
            this.internalInv.setItemDirect(invIndex, ItemStack.parseOptional(registries, item));
        });
    }

    public void add(ClusterAssemblerMatrix cluster) {
        ((HelperAssemblerMatrixModifier) cluster).eaep$addCrafter(this);
    }

    public TickingRequest getTickingRequest(IGridNode node) {
        boolean isAwake = false;

        for (int threadIndex = 0; threadIndex < this.currentThread; threadIndex++) {
            var thread = this.threads[threadIndex];
            thread.recalculatePlan();
            thread.updateSleepiness();
            isAwake |= thread.isAwake();
        }

        return new TickingRequest(1, 1, !isAwake);
    }

    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.cluster == null) {
            return TickRateModulation.SLEEP;
        } else {
            this.calculateCurrentThread();

            TickRateModulation rate = TickRateModulation.SLEEP;

            for (int threadIndex = 0; threadIndex < this.currentThread; threadIndex++) {
                var thread = this.threads[threadIndex];
                if (!thread.isAwake()) continue;

                var currentRate = thread.tick(Math.min(this.cluster.getSpeedCore(), 5), ticksSinceLastCall);
                if (currentRate.ordinal() > rate.ordinal())
                    rate = currentRate;
            }

            ((HelperAssemblerMatrixModifier) this.cluster).eaep$updateCrafter(this);
            return rate;
        }
    }

    public void saveChangedInventory(AppEngInternalInventory inv) {
        for (CraftingThread t : this.threads) {
            if (inv == t.getInternalInventory()) {
                t.recalculatePlan();
                break;
            }
        }

        this.saveChanges();
    }

    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (ItemStack stack : this.internalInv) {
            GenericStack genericStack = GenericStack.unwrapItemStack(stack);
            if (genericStack != null) {
                genericStack.what().addDrops(genericStack.amount(), drops, level, pos);
            } else {
                drops.add(stack);
            }
        }

    }

    public void clearContent() {
        super.clearContent();
        this.internalInv.clear();
    }

    private void calculateCurrentThread() {
        var multiplier = Math.floorDiv(cluster.getSpeedCore(), 5);
        var amplification = EAEPConfig.coreCrafterThreadAmplification.getAsInt();
        var baseThreads = EAEPConfig.baseCoreCrafterThreads.getAsInt();
        this.currentThread = Math.min(multiplier * amplification + baseThreads, getMaxThread());
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.CORE_ADVANCED_CRAFTER.get();
    }

    public static int getMaxThread() {
        return EAEPConfig.maximumCoreCrafterThreads.getAsInt();
    }
}
