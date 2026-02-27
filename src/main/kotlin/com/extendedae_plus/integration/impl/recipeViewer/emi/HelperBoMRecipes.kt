package com.extendedae_plus.integration.impl.recipeViewer.emi

import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiResolutionRecipe
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.bom.FoldState
import dev.emi.emi.bom.MaterialNode
import net.minecraft.resources.ResourceLocation

object HelperBoMRecipes {
    fun mapChildren(nodeParent: MaterialNode, batches: Long): HashMap<ResourceLocation, EmiStack> {
        val recipe = nodeParent.recipe ?: return HashMap()
        if (nodeParent.state != FoldState.EXPANDED) return HashMap()
        val children = nodeParent.children ?: return HashMap()

        val resultMapped = HashMap<ResourceLocation, EmiStack>()

        children.forEach { child ->
            val stack = batchAmount(child, batches)
            recipe.inputs.forEach { input ->
                if (!input.emiStacks.contains(stack)) return@forEach
                resultMapped[input.emiStacks[0].id] = stack
            }
        }

        return resultMapped
    }

    fun applyMappings(
        adaptable: EmiRecipeAdaptable,
        original: EmiRecipe,
        mapper: HashMap<ResourceLocation, EmiStack>
    ) {
        val mapped = ArrayList<EmiIngredient>()
        original.inputs.forEach { input ->
            val key = input.emiStacks[0].id
            val stack = mapper[key]
            mapped.add(stack ?: input)
        }
        adaptable.inputs = mapped
    }

    fun batchAmount(node: MaterialNode, batches: Long): EmiStack {
        val recipe = node.recipe
        val original = if (recipe is EmiResolutionRecipe) recipe.stack
        else node.ingredient.emiStacks[0]
        return original.copy().setAmount(node.amount * batches)
    }
}
