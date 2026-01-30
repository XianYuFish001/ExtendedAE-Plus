package com.extendedae_plus.mixin.impl;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.mixin.bridge.HelperCraftingJob;
import com.extendedae_plus.mixin.core.advancedae.accessor.AccessorAdvCraftingCPULogic;
import com.extendedae_plus.mixin.core.advancedae.accessor.AccessorAdvExecutingCraftingJob;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorCraftingCPULogic;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorExecutingCraftingJob;
import net.minecraft.util.Tuple;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class HolderCardAutoCompletionState {
    private final IManagedGridNode mainNode;
    private final Supplier<IUpgradeInventory> getterUpgradeInventory;

    private boolean cardAvailable = false;

    public static final HolderCardAutoCompletionState EMPTY = new HolderCardAutoCompletionState(
            null, null) {
        @Override public boolean cardAvailable() {
            return false;
        }
        @Override public void onUpgradesChanged() {}
    };

    public HolderCardAutoCompletionState(IManagedGridNode mainNode, Supplier<IUpgradeInventory> getterUpgradeInventory) {
        this.mainNode = mainNode;
        this.getterUpgradeInventory = getterUpgradeInventory;
    }

    public boolean cardAvailable() {
        return this.cardAvailable;
    }

    public void onUpgradesChanged() {
        var upgradesInv = this.getterUpgradeInventory.get();
        if (upgradesInv == null) return;
        var state = new AtomicBoolean(false);
        upgradesInv.forEach(card -> {
            if (!state.get() && card.is(ModItems.CARD_AUTO_COMPLETION))
                state.set(true);
        });
        this.cardAvailable = state.get();
    }

    public void completeJob(IPatternDetails patternDetails) {
        if (!this.cardAvailable()) return;
        if (patternDetails == null) return;

        var node = this.mainNode.getNode();
        if (node == null) return;
        var grid = node.getGrid();
        if (grid == null) return;
        var craftingService = grid.getCraftingService();
        if (craftingService == null) return;

        var taskResult = getTasks(craftingService);
        var tasks = taskResult.getB();
        if (tasks.isEmpty()) return;

        var progress = tasks.get(patternDetails);
        if (progress == null) {
            for (var progressEntry : tasks.entrySet()) {
                if (!testProgress(patternDetails, progressEntry.getKey())) continue;
                progress = progressEntry.getValue();
                break;
            }
        }

        if (progress == null) return;
        if (progress.getValue() > 1) return;
        taskResult.getA().run();
    }

    private static Tuple<Runnable, Map<IPatternDetails, HelperCraftingJob.HelperJobProgress>>
    getTasks(ICraftingService craftingService) {
        Tuple<Runnable, Map<IPatternDetails, HelperCraftingJob.HelperJobProgress>> result =
                new Tuple<>(() -> {}, new HashMap<>());
        craftingService.getCpus().forEach(craftingCPU -> {
            if (!craftingCPU.isBusy()) return;

            result.setA(craftingCPU::cancelJob);
            if (craftingCPU instanceof CraftingCPUCluster cpu
                    && cpu.craftingLogic instanceof AccessorCraftingCPULogic accessorLogic
                    && accessorLogic.getJob() instanceof AccessorExecutingCraftingJob accessorJob) {
                result.getB().putAll(accessorJob.getTasks());
            } else if (craftingCPU instanceof AdvCraftingCPU cpu
                    && cpu.craftingLogic instanceof AccessorAdvCraftingCPULogic accessorLogic
                    && accessorLogic.getJob() instanceof AccessorAdvExecutingCraftingJob accessorJob) {
                result.getB().putAll(accessorJob.getTasks());
            }
        });
        return result;
    }

    private static boolean testProgress(IPatternDetails targetPattern, IPatternDetails taskPattern) {
        if (taskPattern == targetPattern) return true;
        if (taskPattern != null && targetPattern != null) {
            var taskDefinition = taskPattern.getDefinition();
            return taskDefinition != null && taskDefinition.equals(targetPattern.getDefinition());
        }
        return false;
    }
}
