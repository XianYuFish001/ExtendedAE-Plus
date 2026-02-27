package com.extendedae_plus.common.registry.dataComponent

import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.init.EAEPItems
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.common.crafting.DataComponentIngredient

// 搞不懂为什么不自己注册, 非要全塞到customData里
@JvmRecord
data class DataTickingCard(val multiplier: Int, val maxMultiplier: Int) {
    fun toIngredient(multiplier: Int = this.multiplier, maxMultiplier: Int = this.maxMultiplier): Ingredient =
        DataComponentIngredient.of(
            false,
            EAEPDataComponents.CardTicking,
            DataTickingCard(multiplier, maxMultiplier),
            EAEPItems.CardTicking
        )

    fun toStack(multiplier: Int = this.multiplier, maxMultiplier: Int = this.maxMultiplier): ItemStack {
        val stack = EAEPItems.CardTicking.toStack()
        stack.set(EAEPDataComponents.CardTicking, DataTickingCard(multiplier, maxMultiplier))
        return stack
    }

    companion object {
        val codec: Codec<DataTickingCard> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("multiplier").forGetter(DataTickingCard::multiplier),
                Codec.INT.fieldOf("max_multiplier").forGetter(DataTickingCard::maxMultiplier)
            ).apply(instance, ::DataTickingCard)
        }

        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, DataTickingCard> = StreamCodec.composite(
            ByteBufCodecs.INT, DataTickingCard::multiplier,
            ByteBufCodecs.INT, DataTickingCard::maxMultiplier,
            ::DataTickingCard
        )

        fun toIngredient(multiplier: Int, maxMultiplier: Int): Ingredient =
            DataComponentIngredient.of(
                false,
                EAEPDataComponents.CardTicking,
                DataTickingCard(multiplier, maxMultiplier),
                EAEPItems.CardTicking
            )

        fun toStack(multiplier: Int, maxMultiplier: Int): ItemStack {
            val stack = EAEPItems.CardTicking.toStack()
            stack.set(EAEPDataComponents.CardTicking, DataTickingCard(multiplier, maxMultiplier))
            return stack
        }

        @JvmStatic
        fun fromStack(stack: ItemStack) = stack.get(EAEPDataComponents.CardTicking)
            ?: DataTickingCard(1, 1)
    }
}
