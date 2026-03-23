package com.extendedae_plus.common.impl.pattern

import appeng.api.crafting.PatternDetailsHelper
import appeng.api.implementations.blockentities.PatternContainerGroup
import appeng.api.networking.security.IActionHost
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern
import appeng.helpers.patternprovider.PatternContainer
import appeng.menu.AEBaseMenu
import com.extendedae_plus.EAEPConfig
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern.TileAdvancedPattern
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorAccessMenu
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorEncodingMenu
import com.extendedae_plus.mixin.helper.BridgeProviderList
import com.extendedae_plus.mixin.helper.HelperAssemblerMatrixModifier
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.extension.ifTrue
import com.fish.fishlib.util.keyBuilder.Patterns
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

object PatternUploader {
    @JvmStatic
    fun collectProvider(menu: AEBaseMenu): MutableMap<PatternContainerGroup, MutableList<PatternContainer>> {
        val containers = HashMap<PatternContainerGroup, MutableList<PatternContainer>>()

        val grid = (menu.target as? IActionHost)?.actionableNode?.grid ?: return containers

        grid.machineClasses.forEach { clazzMachine ->
            if (!PatternContainer::class.java.isAssignableFrom(clazzMachine)) return@forEach
            grid.getActiveMachines(clazzMachine).forEach { machine ->
                val container = machine as PatternContainer
                val inv = container.terminalPatternInventory
                var slotsFull = true
                for (indexInv in 0..<inv.size()) {
                    if (!inv.getStackInSlot(indexInv).isEmpty) continue
                    slotsFull = false
                    break
                }
                if (slotsFull) return@forEach
                containers.computeIfAbsent(
                    container.terminalGroup
                ) { ArrayList() }.add(container)
            }
        }

        return containers
    }

    fun uploadFromMenuEncoding(menu: AEBaseMenu, hashGroup: Int) {
        if (menu !is BridgeProviderList) return
        val providers = menu.`eaep$getProviderList`().entries
            .find { it.key.hashCode() == hashGroup } ?: return

        if (menu !is AccessorEncodingMenu) return
        val pattern = menu.slotEncoded.item
        if (!PatternDetailsHelper.isEncodedPattern(pattern)) return

        for (provider in providers.value) {
            if (provider.terminalPatternInventory.addItems(pattern).isEmpty) {
                pattern.count = 0
                menu.slotEncoded.set(ItemStack.EMPTY)
                break
            }
        }
    }

    fun uploadFromInventory(player: ServerPlayer, indexSlot: Int, serial: Long) {
        val menu = player.containerMenu
        if (menu !is AccessorAccessMenu) return

        val provider = menu.getIDMap().get(serial) ?: return

        val slot = menu.getSlot(indexSlot)
        val pattern = slot.item
        if (!PatternDetailsHelper.isEncodedPattern(pattern)) return

        if (provider.getInv().addItems(pattern).isEmpty)
            slot.set(ItemStack.EMPTY)
    }

    @JvmStatic
    fun uploadToMatrix(player: ServerPlayer, menu: AEBaseMenu?): ResultMatrixUploading {
        if (menu !is AccessorEncodingMenu) return ResultMatrixUploading.Failed
        var patternStack = menu.slotEncoded.item

        val pattern = PatternDetailsHelper.decodePattern(patternStack, player.level())
        if (pattern !is IMolecularAssemblerSupportedPattern) return ResultMatrixUploading.Unsupported

        val grid = (menu.target as? IActionHost)?.actionableNode?.grid ?: return ResultMatrixUploading.Failed

        val cores = ArrayList<TileAssemblerMatrixPattern>()
        cores.addAll(grid.getActiveMachines(TileAssemblerMatrixPattern::class.java))
        cores.addAll(grid.getActiveMachines(TileAdvancedPattern::class.java))
        if (cores.isEmpty()) return ResultMatrixUploading.Failed

        cores
            .map(TileAssemblerMatrixPattern::getPatternInventory)
            .any {
                for (stack in it) {
                    if (pattern == PatternDetailsHelper.decodePattern(stack, player.level()))
                        return@any true
                }
                return@any false
            }
            .ifTrue {
                player.displayClientMessage(
                    UtilKeyBuilder.of(Patterns.Message)
                        .addStr("pattern_uploading")
                        .addStr("duplicate_pattern")
                        .build(), false
                )
                return ResultMatrixUploading.Duplicate
            }

        cores
            .groupBy(TileAssemblerMatrixPattern::getCluster)
            .flatMap { (cluster, cores) ->
                if (!EAEPConfig.NeedsUploadingPort
                        || (cluster as? HelperAssemblerMatrixModifier)?.`eaep$hasUploadCore`() != true)
                    return@flatMap emptyList()
                cores
            }.forEach { core ->
                if (patternStack.isEmpty) {
                    menu.slotEncoded.clearStack()
                    return ResultMatrixUploading.Success
                }
                patternStack = core.patternInventory.addItems(patternStack)
            }
        return ResultMatrixUploading.Failed
    }

    enum class ResultMatrixUploading {
        Success, Failed, Unsupported, Duplicate
    }
}
