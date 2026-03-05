package com.extendedae_plus.integration.impl.recipeViewer.jei

import appeng.api.stacks.AEFluidKey
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationAppliedMek
import com.extendedae_plus.integration.impl.recipeViewer.IRecipeViewer
import com.extendedae_plus.mixin.core.recipeViewer.jei.accessor.AccessorBookmarkOverlay
import com.extendedae_plus.util.UtilClient
import com.fish.fishlib.util.extension.cast
import com.fish.fishlib.util.extension.onlyIf
import com.mojang.datafixers.util.Pair
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.ingredients.IIngredientType
import mezz.jei.api.neoforge.NeoForgeTypes
import mezz.jei.api.runtime.IJeiRuntime
import mezz.jei.common.Internal
import mezz.jei.gui.bookmarks.IngredientBookmark
import org.lwjgl.glfw.GLFW
import tamaized.ae2jeiintegration.integration.modules.jei.GenericEntryStackHelper
import java.util.*
import kotlin.jvm.optionals.getOrNull

object ViewerJei : IRecipeViewer {
    internal var runtime: IJeiRuntime? = null

    override fun getHoveredStacks(
        mouseX: Double, mouseY: Double
    ) = this.getHoveredStacks()

    override fun getHoveredStacks(): List<GenericStack> {
        val runtime = this.runtime ?: return Collections.emptyList()

        val overlayList = runtime.ingredientListOverlay
        val overlayBookmark = runtime.bookmarkOverlay

        val stackJei = overlayList.ingredientUnderMouse.getOrNull()
            ?: overlayBookmark.ingredientUnderMouse.getOrNull()
            ?: return Collections.emptyList()
        val stack = GenericEntryStackHelper.ingredientToStack(stackJei)
            ?: return Collections.emptyList()
        return Collections.singletonList(stack)
    }

    override fun getFavorites(): List<GenericStack> {
        val runtime = this.runtime ?: return Collections.emptyList()

        val overlayBookmark = runtime.bookmarkOverlay
                as? AccessorBookmarkOverlay ?: return Collections.emptyList()
        val bookmark = overlayBookmark.getBookmarkList()

        return bookmark.elements
            .mapNotNull { GenericEntryStackHelper.ingredientToStack(it.typedIngredient) }
    }

    override fun getPulled(mouseKey: Int): Pair<Int, Boolean> {
        var amount = 0
        var toInv = true

        if (UtilClient.ctrl()) amount = 1
        if (amount > 0 && UtilClient.shift()) amount = 64

        if (mouseKey == GLFW.GLFW_MOUSE_BUTTON_RIGHT) toInv = false

        return Pair(amount, toInv)
    }

    override fun isCheatMode() = Internal.getClientToggleState().isCheatItemsEnabled

    override fun addFavorite(stack: GenericStack) {
        val key = stack.what
        val appMek = ManagerIntegration<IntegrationAppliedMek>()
        when {
            key is AEItemKey ->
                key.toStack(stack.amount.toInt()) to VanillaTypes.ITEM_STACK

            key is AEFluidKey ->
                key.toStack(stack.amount.toInt()) to NeoForgeTypes.FLUID_STACK

            appMek?.isMekKey(key) ?: false ->
                appMek.getStack(key) to IIngredientType(appMek.TypeChemicalJei)

            else -> return
        }.onlyIf { (stack, type) -> stack != null && type != null }
            ?.let { (stack, type) -> this.addFavorite(stack!!, type.cast()) }
    }

    private fun <TStack : Any> addFavorite(stack: TStack, type: IIngredientType<TStack>) {
        val runtime = this.runtime ?: return
        val overlayBookmark = runtime.bookmarkOverlay
        if (overlayBookmark !is AccessorBookmarkOverlay) return

        runtime.ingredientManager.createTypedIngredient(type, stack).ifPresent {
            overlayBookmark.bookmarkList.add(
                IngredientBookmark.create(it, runtime.ingredientManager)
            )
        }
    }

    override fun setSearch(text: String) = this.runtime?.ingredientFilter?.filterText = text
}