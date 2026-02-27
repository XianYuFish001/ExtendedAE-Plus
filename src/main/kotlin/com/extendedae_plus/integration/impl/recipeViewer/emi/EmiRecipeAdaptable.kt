package com.extendedae_plus.integration.impl.recipeViewer.emi

import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import dev.emi.emi.bom.MaterialNode
import dev.emi.emi.jemi.JemiRecipe

class EmiRecipeAdaptable(recipe: EmiRecipe) : EmiRecipe {
    private val recipe: EmiRecipe

    internal var inputs: List<EmiIngredient>? = null

    init {
        require(recipe !is EmiRecipeAdaptable) { "Cannot wrap a wrapped EmiRecipe" }
        this.recipe = recipe
    }

    fun adapt(nodeParent: MaterialNode, batches: Long) {
        val mapped = HelperBoMRecipes.mapChildren(nodeParent, batches)
        HelperBoMRecipes.applyMappings(this, this.recipe, mapped)
        this.outputs.forEach { it.amount *= batches }
    }

    override fun getInputs(): List<EmiIngredient> = this.inputs ?: this.recipe.inputs

    override fun getOutputs(): List<EmiStack> = this.recipe.outputs

    override fun getCategory(): EmiRecipeCategory = this.recipe.category

    override fun getId() = this.recipe.id

    override fun getCatalysts(): List<EmiIngredient> = this.recipe.catalysts

    override fun getDisplayWidth() = this.recipe.displayWidth

    override fun getDisplayHeight() = this.recipe.displayHeight

    override fun addWidgets(widgets: WidgetHolder) =
        this.recipe.addWidgets(widgets)

    override fun supportsRecipeTree() = this.recipe.supportsRecipeTree()

    override fun hideCraftable() = this.recipe.hideCraftable()

    override fun getBackingRecipe() = this.recipe.backingRecipe

    companion object {
        @JvmStatic
        fun of(recipe: EmiRecipe, nodeParent: MaterialNode, batches: Long): EmiRecipeAdaptable {
            val adaptable = EmiRecipeAdaptable(recipe)
            adaptable.adapt(nodeParent, batches)
            return adaptable
        }

        @JvmStatic
        fun EmiRecipe.unbox() = (this as? EmiRecipeAdaptable)?.recipe ?: this

        @JvmStatic
        fun unboxJemi(boxed: Any): JemiRecipe<*>? = when (boxed) {
            is JemiRecipe<*> -> boxed
            is EmiRecipeAdaptable -> unboxJemi(boxed.recipe)
            else -> null
        }
    }
}
