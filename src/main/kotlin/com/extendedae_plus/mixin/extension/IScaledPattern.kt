package com.extendedae_plus.mixin.extension

import appeng.api.crafting.IPatternDetails
import appeng.api.networking.crafting.ICraftingService
import appeng.api.stacks.GenericStack
import appeng.me.service.CraftingService
import com.extendedae_plus.EAEPConfig
import com.extendedae_plus.common.impl.pattern.smartDoubling.HolderCraftingAmount
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorNetworkProviders.AccessorProviderList
import com.google.common.math.LongMath
import java.math.RoundingMode
import java.util.*
import kotlin.math.min

interface IScaledPattern : IPatternDetails {
    val `eaep$multiplier`: Long

    val `eaep$enabled`: Boolean

    fun `eaep$setEnabled`(value: Boolean)

    fun `eaep$getInputs`(): List<GenericStack>

    fun `eaep$getOutputs`(): List<GenericStack>

    /**
     * @param multiplier Negative: Divide
     */
    fun `eaep$create`(multiplier: Long, saveInfo: Boolean): IPatternDetails?

    fun `eaep$create`(multiplier: Long) = `eaep$create`(multiplier, true)

    fun `eaep$create`(iServiceCrafting: ICraftingService): IPatternDetails? {
        var amountRequested = HolderCraftingAmount.get()
        HolderCraftingAmount.pop()
        if (amountRequested < 1) return null

        val multiplierMax: Int = EAEPConfig.SmartDoublingMaxMultiplier
        if (multiplierMax > 0) amountRequested = min(multiplierMax.toLong(), amountRequested)

        if (!EAEPConfig.ProviderRoundRobin) return this.`eaep$create`(amountRequested)

        if (iServiceCrafting !is CraftingService) return null

        val sizeProviders = (iServiceCrafting.getProviders(this) as AccessorProviderList)
            .getProviders()
            .size
        if (sizeProviders < 2) return this.`eaep$create`(amountRequested)

        amountRequested = Math.ceilDiv(amountRequested, sizeProviders)
        if (amountRequested < 1) return null
        return this.`eaep$create`(amountRequested)
    }

    companion object {
        @JvmStatic
        @Throws(ArithmeticException::class)
        fun process(target: List<GenericStack?>, multiplier: Long): List<GenericStack?> {
            val multiplied = ArrayList<GenericStack?>()
            target.forEach { stack ->
                if (stack == null) {
                    multiplied.add(null)
                    return@forEach
                }
                val amountMultiplied: Long
                try {
                    amountMultiplied = if (multiplier >= 0)
                        LongMath.saturatedMultiply(stack.amount(), multiplier)
                    else
                        LongMath.divide(stack.amount(), -multiplier, RoundingMode.UNNECESSARY)
                } catch (_: ArithmeticException) {
                    return@forEach
                }
                if (amountMultiplied == 0L) return@forEach
                multiplied.add(GenericStack(stack.what(), amountMultiplied))
            }
            if (multiplied.size != target.size) throw ArithmeticException("Failed to divide pattern")
            return Collections.unmodifiableList(multiplied)
        }
    }
}
