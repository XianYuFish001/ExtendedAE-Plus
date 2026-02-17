package com.extendedae_plus.mixin.impl;

import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.network.clientbound.CraftingJobStatusPacket;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.mixin.bridge.HelperCraftingJob;
import com.extendedae_plus.mixin.core.advancedae.accessor.AccessorAdvCraftingCPULogic;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorCraftingCPULogic;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.extendedae_plus.util.extension.ExtensionScaledPattern;
import lombok.experimental.ExtensionMethod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@ExtensionMethod(ExtensionScaledPattern.class)
public class HelperAutoCompletion {
    private final IManagedGridNode mainNode;
    private final Supplier<IUpgradeInventory> getterUpgradeInventory;
    private Supplier<BlockEntity> blockEntity;

    private boolean cardAvailable = false;

    public static final HelperAutoCompletion EMPTY = new HelperAutoCompletion(
            null, null, null) {
        @Override
        public boolean cardAvailable() {
            return false;
        }

        @Override
        public void onUpgradesChanged() {
        }
    };

    public HelperAutoCompletion(IManagedGridNode mainNode,
                                Supplier<IUpgradeInventory> getterUpgradeInventory,
                                Supplier<BlockEntity> blockEntity) {
        this.mainNode = mainNode;
        this.getterUpgradeInventory = getterUpgradeInventory;
        this.blockEntity = blockEntity;
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

    public void completeJob(IPatternDetails patternTarget) {
        if (!this.cardAvailable()) return;
        if (patternTarget == null) return;

        var node = this.mainNode.getNode();
        if (node == null) return;
        var grid = node.getGrid();
        if (grid == null) return;
        var craftingService = grid.getCraftingService();
        if (craftingService == null) return;

        this.getHelperCrafting(craftingService).forEach((helper, canceler) -> {
            var tasks = helper.getTasks();
            if (tasks.isEmpty()) return;

            var value = tasks.entrySet().stream()
                    .filter(entry -> {
                        var pattern = entry.getKey().original();
                        var target = patternTarget.original();

                        if (pattern == null || target == null) return false;
                        return target.equals(pattern);
                    })
                    .filter(entry -> entry.getValue() != null
                            && entry.getValue().getValue() <= 1)
                    .findAny()
                    .orElse(null);
            if (value == null) return;

            var blockEntity = this.blockEntity.get();
            if (blockEntity == null) return;

            if (!(blockEntity.getLevel() instanceof ServerLevel level)) return;
            var player = IPlayerRegistry.getConnected(
                    level.getServer(),
                    helper.getPlayerID()
            );
            if (player == null) return;

            var result = value.getKey().getPrimaryOutput().what();
            if (!helper.getOutputFinal().what().equals(result)) {
                player.displayClientMessage(
                        UtilKeyBuilder.of(UtilKeyBuilder.message)
                                .addStr("auto_completion")
                                .addStr("non_tail")
                                .args("[" + result.getDisplayName().getString() + "]")
                                .build(),
                        false
                );
                return;
            }

            canceler.run();

            PacketDistributor.sendToPlayer(player, new CraftingJobStatusPacket(
                    helper.getLink().getCraftingID(),
                    helper.getOutputFinal().what(),
                    helper.getOutputFinal().amount(),
                    helper.getRemainingAmount(),
                    CraftingJobStatusPacket.Status.FINISHED
            ));
        });
    }

    private HashMap<HelperCraftingJob, Runnable>
    getHelperCrafting(ICraftingService serviceCrafting) {
        var result = new HashMap<HelperCraftingJob, Runnable>();
        serviceCrafting.getCpus().forEach(cpu -> {
            if (!cpu.isBusy()) return;

            HelperCraftingJob helper = null;
            if (cpu instanceof CraftingCPUCluster cpuVanilla
                    && cpuVanilla.craftingLogic instanceof AccessorCraftingCPULogic logicVanilla
                    && logicVanilla.getJob() instanceof HelperCraftingJob helperVanilla)
                helper = helperVanilla;
            else if (cpu instanceof AdvCraftingCPU cpuAdv
                    && cpuAdv.craftingLogic instanceof AccessorAdvCraftingCPULogic logicAdv
                    && logicAdv.getJob() instanceof HelperCraftingJob helperAdv)
                helper = helperAdv;
            if (helper == null) return;

            result.put(helper, cpu::cancelJob);
        });
        return result;
    }

//    private static Tuple<Runnable, HelperCraftingJob>
//    getTasks(ICraftingService craftingService) {
//        var result =
//                new Tuple<Runnable, HelperCraftingJob>(() -> {}, null);
//        craftingService.getCpus().forEach(craftingCPU -> {
//            if (!craftingCPU.isBusy()) return;
//
//            result.setA(craftingCPU::cancelJob);
//            if (craftingCPU instanceof CraftingCPUCluster cpu
//                    && cpu.craftingLogic instanceof AccessorCraftingCPULogic accessorLogic
//                    && accessorLogic.getJob() instanceof AccessorExecutingCraftingJob accessorJob) {
//                result.setB(accessorJob);
//            } else if (craftingCPU instanceof AdvCraftingCPU cpu
//                    && cpu.craftingLogic instanceof AccessorAdvCraftingCPULogic accessorLogic
//                    && accessorLogic.getJob() instanceof AccessorAdvExecutingCraftingJob accessorJob) {
//                result.setB(accessorJob);
//            }
//        });
//        return result;
//    }
//
//    private static boolean testProgress(IPatternDetails targetPattern, IPatternDetails taskPattern) {
//        if (taskPattern == targetPattern) return true;
//        if (taskPattern != null && targetPattern != null) {
//            targetPattern = targetPattern.original();
//            taskPattern = taskPattern.original();
//
//            var taskDefinition = taskPattern.getDefinition();
//            return taskDefinition != null && taskDefinition.equals(targetPattern.getDefinition());
//        }
//        return false;
//    }
}
