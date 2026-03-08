package com.extendedae_plus.integration.impl.recipeViewer.emi

import appeng.api.stacks.GenericStack
import appeng.integration.modules.emi.EmiStackHelper
import com.extendedae_plus.integration.impl.recipeViewer.IRecipeViewer
import com.mojang.datafixers.util.Pair
import dev.emi.emi.api.EmiApi
import dev.emi.emi.config.EmiConfig
import dev.emi.emi.runtime.EmiFavorite
import dev.emi.emi.runtime.EmiFavorites

object ViewerEmi : IRecipeViewer {
    override fun getHoveredStacks(mouseX: Double, mouseY: Double) =
        EmiApi.getHoveredStack(mouseX.toInt(), mouseY.toInt(), false)
            .stack.emiStacks
            .mapNotNull(EmiStackHelper::toGenericStack)

    override fun getHoveredStacks() =
        EmiApi.getHoveredStack(false)
            .stack.emiStacks
            .mapNotNull(EmiStackHelper::toGenericStack)

    override fun getFavorites() =
        EmiFavorites.favorites
            .flatMap(EmiFavorite::getEmiStacks)
            .mapNotNull(EmiStackHelper::toGenericStack)

    override fun getPulled(mouseKey: Int) = when {
        EmiConfig.cheatOneToCursor.matchesMouse(mouseKey) -> Pair(1, false)
        EmiConfig.cheatOneToInventory.matchesMouse(mouseKey) -> Pair(1, true)
        EmiConfig.cheatStackToCursor.matchesMouse(mouseKey) -> Pair(64, false)
        EmiConfig.cheatStackToInventory.matchesMouse(mouseKey) -> Pair(64, true)
        else -> Pair(0, false)
    }

    override fun isCheatMode() = EmiApi.isCheatMode()

    override fun addFavorite(stack: GenericStack) =
        EmiFavorites.addFavorite(EmiStackHelper.toEmiStack(stack))

    override fun setSearch(text: String) = EmiApi.setSearchText(text)
}
