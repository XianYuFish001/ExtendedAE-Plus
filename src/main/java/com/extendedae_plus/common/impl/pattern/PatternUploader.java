package com.extendedae_plus.common.impl.pattern;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.security.IActionHost;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.AEBaseMenu;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern.BlockEntityAdvancedPattern;
import com.extendedae_plus.mixin.bridge.BridgeProviderList;
import com.extendedae_plus.mixin.bridge.HelperAssemblerMatrixModifier;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorAccessMenu;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorEncodingMenu;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class PatternUploader {
    public static Map<PatternContainerGroup, List<PatternContainer>> collectProvider(AEBaseMenu menu) {
        var containers = new HashMap<PatternContainerGroup, List<PatternContainer>>();

        var grid = menu.getTarget() instanceof IActionHost actionHost
                && actionHost.getActionableNode() != null
                ? actionHost.getActionableNode().getGrid() : null;
        if (grid == null) return containers;

        grid.getMachineClasses().forEach(clazzMachine -> {
            if (!PatternContainer.class.isAssignableFrom(clazzMachine)) return;
            grid.getActiveMachines(clazzMachine).forEach(machine -> {
                var container = (PatternContainer) machine;

                var inv = container.getTerminalPatternInventory();
                var slotsFull = true;
                for (int indexInv = 0; indexInv < inv.size(); indexInv++) {
                    if (!inv.getStackInSlot(indexInv).isEmpty()) continue;
                    slotsFull = false;
                    break;
                }
                if (slotsFull) return;

                containers.computeIfAbsent(container.getTerminalGroup(),
                                $ -> new ArrayList<>())
                        .add(container);
            });
        });

        return containers;
    }

    public static void uploadFromMenuEncoding(AEBaseMenu menu, int hashGroup) {
        if (!(menu instanceof BridgeProviderList bridge)) return;
        var providers = bridge.eaep$getProviderList().entrySet().stream()
                .filter(entry -> entry.getKey().hashCode() == hashGroup)
                .findAny();
        if (providers.isEmpty()) return;

        if (!(menu instanceof AccessorEncodingMenu accessor)) return;
        var slot = accessor.getSlotEncoded();
        var pattern = slot.getItem();
        if (!PatternDetailsHelper.isEncodedPattern(pattern)) return;

        for (var provider : providers.get().getValue()) {
            if (provider.getTerminalPatternInventory()
                    .addItems(pattern).isEmpty()) {
                pattern.setCount(0);
                accessor.getSlotEncoded().set(ItemStack.EMPTY);
            }
        }
    }

    public static void uploadFromInventory(ServerPlayer player, int indexSlot, long serial) {
        if (!(player.containerMenu instanceof AccessorAccessMenu accessor)) return;

        var provider = accessor.getIDMap().get(serial);
        if (provider == null) return;

        var slot = player.containerMenu.getSlot(indexSlot);
        var pattern = slot.getItem();
        if (!PatternDetailsHelper.isEncodedPattern(pattern)) return;

        if (provider.getInv().addItems(pattern).isEmpty())
            slot.set(ItemStack.EMPTY);
    }

    /**
     * @return - True:  succeed
     * - False: failed
     * - Null:  duplicate
     */
    public static ResultUploadMatrix uploadToMatrix(ServerPlayer player, AEBaseMenu menu) {
        if (!(menu instanceof AccessorEncodingMenu accessor)) return ResultUploadMatrix.FAILED;
        var patternStack = accessor.getSlotEncoded().getItem();
        if (!(PatternDetailsHelper.decodePattern(patternStack, player.level())
                instanceof IMolecularAssemblerSupportedPattern patternDetails))
            return ResultUploadMatrix.NON_CRAFTING;

        var grid = menu.getTarget() instanceof IActionHost actionHost
                && actionHost.getActionableNode() != null
                ? actionHost.getActionableNode().getGrid() : null;
        if (grid == null) return ResultUploadMatrix.FAILED;

        var cores = new ArrayList<TileAssemblerMatrixPattern>();
        cores.addAll(grid.getActiveMachines(TileAssemblerMatrixPattern.class));
        cores.addAll(grid.getActiveMachines(BlockEntityAdvancedPattern.class));
        if (cores.isEmpty()) return ResultUploadMatrix.FAILED;

        var byCluster = cores.stream().collect(Multimaps.toMultimap(
                TileAssemblerMatrixPattern::getCluster,
                Function.identity(),
                () -> MultimapBuilder.hashKeys().arrayListValues().build()
        )).asMap();

        for (var entry : byCluster.entrySet()) {
            var cluster = entry.getKey();
            var coresGrouped = entry.getValue();

            if (EAEPConfig.needsUploadingPort.get()
                    && !(cluster instanceof HelperAssemblerMatrixModifier helper
                    && helper.eaep$hasUploadCore()))
                continue;

            if (cores.stream()
                    .map(TileAssemblerMatrixPattern::getPatternInventory)
                    .anyMatch(inv -> {
                        for (var stack : inv) {
                            if (patternDetails.equals(
                                    PatternDetailsHelper.decodePattern(stack, player.level())))
                                return true;
                        }
                        return false;
                    })) {
                player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.message)
                        .addStr("pattern_uploading")
                        .addStr("duplicate_pattern")
                        .build(), false);
                return ResultUploadMatrix.DUPLICATE;
            }

            for (var core : coresGrouped) {
                if (patternStack.isEmpty()) {
                    accessor.getSlotEncoded().clearStack();
                    break;
                }
                patternStack = core.getPatternInventory().addItems(patternStack);
            }
        }

        return patternStack.isEmpty() ? ResultUploadMatrix.SUCCESS : ResultUploadMatrix.FAILED;
    }

    public enum ResultUploadMatrix {
        SUCCESS, FAILED, DUPLICATE, NON_CRAFTING, NO_CORE
    }
}
