package com.extendedae_plus.common.init

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter.BlockAdvancedCrafter
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern.BlockAdvancedPattern
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedSpeed.BlockAdvancedSpeed
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver
import com.fish.fishlib.common.InitObject
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister

/** CraftingUnit Blocks are now in [com.extendedae_plus.common.registry.block.EAEPCraftingUnit.Blocks] */
object EAEPBlocks {
    @InitObject
    val Register: DeferredRegister.Blocks = DeferredRegister.createBlocks(ExtendedAEPlus.MODID)

    @JvmField
    val Blocks: MutableList<DeferredBlock<out Block>> = ArrayList()

    @JvmField
    val WirelessTransceiver: DeferredBlock<BlockWirelessTransceiver> =
        this.register("wireless_transceiver", ::BlockWirelessTransceiver)
    @JvmField
    val PortUpload: DeferredBlock<BlockUpload> =
        this.register("assembler_matrix_upload", ::BlockUpload)
    @JvmField
    val CoreAdvancedCrafter: DeferredBlock<BlockAdvancedCrafter> =
        this.register("assembler_matrix_advanced_crafter", ::BlockAdvancedCrafter)
    @JvmField
    val CoreAdvancedPattern: DeferredBlock<BlockAdvancedPattern> =
        this.register("assembler_matrix_advanced_pattern", ::BlockAdvancedPattern)
    @JvmField
    val CoreAdvancedSpeed: DeferredBlock<BlockAdvancedSpeed> =
        this.register("assembler_matrix_advanced_speed", ::BlockAdvancedSpeed)

    fun <TBlock : Block> register(
        name: String, constructor: () -> TBlock
    ): DeferredBlock<TBlock> {
        val block = Register.register(name, constructor)
        Blocks.add(block)
        return block
    }
}