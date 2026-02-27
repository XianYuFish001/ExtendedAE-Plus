package com.extendedae_plus.mixin.helper

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.GenericStack
import appeng.crafting.CraftingLink

interface HelperCraftingJob {
    val tasks: MutableMap<IPatternDetails, HelperJobProgress>
    val link: CraftingLink
    val outputFinal: GenericStack
    val playerID: Int?
    val remainingAmount: Long

    interface HelperJobProgress {
        val value: Long
    }
}
