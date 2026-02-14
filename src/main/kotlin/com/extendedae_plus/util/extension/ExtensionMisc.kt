package com.extendedae_plus.util.extension

import appeng.api.stacks.AEItemKey
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorItemKey
import com.extendedae_plus.mixin.core.extendedae.accessor.AccessorBuilderCrystalAssembler
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder
import com.glodblock.github.glodium.recipe.stack.IngredientStack
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.crafting.Ingredient

private typealias UnaryOperator<T> = (T) -> T

fun <TType : Any> AEItemKey.set(type: DataComponentType<TType>, value: TType): AEItemKey {
    val stack = this.toStack()
    stack.set(type, value)
    return AccessorItemKey.`eaep$newInstance`(stack)
}

fun <TType : Any> AEItemKey.update(type: DataComponentType<TType>, default: TType, updater: UnaryOperator<TType>): AEItemKey {
    val stack = this.toStack()
    val value = stack.getOrDefault(type, default)
    stack.set(type, updater(value))
    return AccessorItemKey.`eaep$newInstance`(stack)
}

fun CrystalAssemblerRecipeBuilder.input(ingredient: Ingredient, amount: Int): CrystalAssemblerRecipeBuilder {
    (this as AccessorBuilderCrystalAssembler).inputs.add(IngredientStack.of(ingredient, amount))
    return this
}

fun EmiRecipe.isNonProcessing() = this == VanillaEmiRecipeCategories.CRAFTING
        || this == VanillaEmiRecipeCategories.STONECUTTING
        || this == VanillaEmiRecipeCategories.SMITHING

fun <T> MutableCollection<T>.addAll(vararg elements: T) = elements.forEach(this::add)

fun <T> T.onlyIf(predicate: T.() -> Boolean) = if (predicate(this)) this else null

@Suppress("unchecked_cast")
fun <T> Any?.cast() = this as T