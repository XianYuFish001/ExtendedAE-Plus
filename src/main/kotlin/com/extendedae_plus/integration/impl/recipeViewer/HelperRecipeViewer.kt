package com.extendedae_plus.integration.impl.recipeViewer

import appeng.api.stacks.GenericStack
import com.extendedae_plus.integration.helper.ContextModLoaded
import com.extendedae_plus.integration.impl.recipeViewer.emi.ViewerEmi
import com.extendedae_plus.integration.impl.recipeViewer.jei.ViewerJei
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
object HelperRecipeViewer {
    private val viewer by lazy {
        if (ContextModLoaded.Emi()) ViewerEmi
        else if (ContextModLoaded.Jei()) ViewerJei
        else ViewerEmpty
    }

    @JvmStatic
    fun getHoveredStacks() = this.viewer.getHoveredStacks()

    @JvmStatic
    fun getFavorites() = this.viewer.getFavorites()

    @JvmStatic
    fun getPulled(mouseKey: Int) = this.viewer.getPulled(mouseKey)

    @JvmStatic
    fun isCheatMode() = this.viewer.isCheatMode()

    @JvmStatic
    fun addFavorite(stack: GenericStack) = this.viewer.addFavorite(stack)

    @JvmStatic
    fun setSearchText(text: String) = this.viewer.setSearch(text)
}
