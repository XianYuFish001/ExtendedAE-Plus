package com.extendedae_plus.integration.impl.recipeViewer

import appeng.api.stacks.GenericStack
import com.mojang.datafixers.util.Pair
import java.util.*

object ViewerEmpty : IRecipeViewer {
    override fun getHoveredStacks(mouseX: Double, mouseY: Double) = this.getHoveredStacks()

    override fun getHoveredStacks(): List<GenericStack> = Collections.emptyList()

    override fun getFavorites(): List<GenericStack> = Collections.emptyList()

    override fun getPulled(mouseKey: Int) = Pair(0, false)

    override fun isCheatMode() = true

    override fun addFavorite(stack: GenericStack) = Unit

    override fun setSearch(text: String) = Unit
}
