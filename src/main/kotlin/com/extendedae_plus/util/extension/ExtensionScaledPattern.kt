package com.extendedae_plus.util.extension

import appeng.api.config.YesNo
import appeng.api.crafting.IPatternDetails
import appeng.api.networking.crafting.ICraftingService
import appeng.crafting.pattern.AEProcessingPattern
import com.extendedae_plus.mixin.extension.IScaledPattern
import net.minecraft.world.item.ItemStack
import net.pedroksl.advanced_ae.common.patterns.AdvProcessingPattern
import java.util.*

object ExtensionScaledPattern {
    fun IPatternDetails.unpack(containsUnavailable: Boolean): Optional<IScaledPattern> {
        if (this !is IScaledPattern || !(containsUnavailable || this.`eaep$enabled`))
            return Optional.empty()
        return Optional.of(this)
    }

    @JvmStatic
    fun IPatternDetails.writeToStack(stack: ItemStack): Unit = when (this) {
        is AdvProcessingPattern -> AdvProcessingPattern.encode(
            stack,
            this.sparseInputs,
            this.sparseOutputs,
            this.directionMap
        )

        is IScaledPattern -> AEProcessingPattern.encode(
            stack,
            this.`eaep$getInputs`(),
            this.`eaep$getOutputs`()
        )

        else -> Unit
    }


    @JvmStatic
    fun IPatternDetails.create(serviceCrafting: ICraftingService) =
        this.unpack(true)
            .map { it.`eaep$create`(serviceCrafting) }
            .orElse(this)

    @JvmStatic
    fun IPatternDetails.create(multiplier: Long, saveInfo: Boolean = true) =
        this.unpack(true)
            .map { it.`eaep$create`(multiplier, saveInfo) }
            .orElse(this)

    @JvmStatic
    fun setState(enabled: YesNo) = setter@{ pattern: IPatternDetails? ->
        if (pattern !is IScaledPattern) return@setter
        pattern.`eaep$setEnabled`(enabled == YesNo.YES)
    }
}
