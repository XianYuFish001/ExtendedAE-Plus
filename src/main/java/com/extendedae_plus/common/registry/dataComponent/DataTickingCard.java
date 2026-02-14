package com.extendedae_plus.common.registry.dataComponent;

import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.init.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

// 搞不懂为什么不自己注册, 非要全塞到customData里
public record DataTickingCard(int multiplier, int maxMultiplier) {
    public static final Codec<DataTickingCard> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("multiplier").forGetter(DataTickingCard::multiplier),
            Codec.INT.fieldOf("max_multiplier").forGetter(DataTickingCard::maxMultiplier)
    ).apply(inst, DataTickingCard::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataTickingCard> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DataTickingCard::multiplier,
            ByteBufCodecs.INT, DataTickingCard::maxMultiplier,
            DataTickingCard::new
    );

    public ItemStack toStack() {
        return toStack(this.multiplier, this.maxMultiplier);
    }

    public Ingredient toIngredient() {
        return toIngredient(this.multiplier, this.maxMultiplier);
    }

    public static Ingredient toIngredient(int multiplier, int maxMultiplier) {
        return DataComponentIngredient.of(false,
                ModDataComponents.CardTicking,
                new DataTickingCard(multiplier, maxMultiplier),
                ModItems.CardTicking);
    }

    public static ItemStack toStack(int multiplier, int maxMultiplier) {
        var stack = ModItems.CardTicking.toStack();
        stack.set(ModDataComponents.CardTicking, new DataTickingCard(multiplier, maxMultiplier));
        return stack;
    }

    public static DataTickingCard fromStack(ItemStack stack) {
        if (!stack.has(ModDataComponents.CardTicking)) return new DataTickingCard(1, 1);
        return stack.get(ModDataComponents.CardTicking);
    }
}
