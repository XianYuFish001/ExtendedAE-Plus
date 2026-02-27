package com.extendedae_plus.integration.impl.recipeViewer

import appeng.api.stacks.GenericStack
import com.mojang.datafixers.util.Pair

interface IRecipeViewer {
    fun getHoveredStacks(mouseX: Double, mouseY: Double): List<GenericStack>

    fun getHoveredStacks(): List<GenericStack>

    fun getFavorites(): List<GenericStack>

    fun getPulled(mouseKey: Int): Pair<Int, Boolean>

    fun isCheatMode() = true

    fun addFavorite(stack: GenericStack)

    fun setSearch(text: String)
}
