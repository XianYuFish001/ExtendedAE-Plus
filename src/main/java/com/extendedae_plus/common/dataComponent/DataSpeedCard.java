package com.extendedae_plus.common.dataComponent;

import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.init.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

// 搞不懂为什么不自己注册, 非要全塞到customData里
public record DataSpeedCard(int multiplier) {
    public static final Codec<DataSpeedCard> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("multiplier").forGetter(DataSpeedCard::multiplier)
    ).apply(inst, DataSpeedCard::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataSpeedCard> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DataSpeedCard::multiplier,
            DataSpeedCard::new
    );

    public static Ingredient toIngredient(int multiplier) {
        return Ingredient.of(toStack(multiplier));
    }

    public static ItemStack toStack(int multiplier) {
        var stack = ModItems.ENTITY_SPEED_CARD.toStack();
        stack.set(ModDataComponents.DATA_SPEED_CARD, new DataSpeedCard(multiplier));
        return stack;
    }

    public static int fromStack(ItemStack stack) {
        if (!stack.has(ModDataComponents.DATA_SPEED_CARD)) return 1;
        var data = stack.get(ModDataComponents.DATA_SPEED_CARD);
        return data.multiplier;
    }
}
