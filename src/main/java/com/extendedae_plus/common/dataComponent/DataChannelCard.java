package com.extendedae_plus.common.dataComponent;

import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.init.ModDataComponents;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record DataChannelCard(long frequency, @Nullable UUID owner, String ownerName) {
    public static final Codec<DataChannelCard> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.LONG.fieldOf("frequency").forGetter(DataChannelCard::frequency),
            UUIDUtil.CODEC.lenientOptionalFieldOf("owner")
                    .forGetter(data -> Optional.ofNullable(data.owner())),
            Codec.STRING.fieldOf("owner_name").forGetter(DataChannelCard::ownerName)
    ).apply(inst, (frequency, owner, ownerName) ->
            new DataChannelCard(frequency, owner.orElse(null), ownerName)));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataChannelCard> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, DataChannelCard::frequency,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.owner()),
            ByteBufCodecs.STRING_UTF8, DataChannelCard::ownerName,
            (frequency, owner, ownerName) ->
                    new DataChannelCard(frequency, owner.orElse(null), ownerName)
    );

    public static void copyFromTransceiver(BlockEntityWirelessTransceiver transceiver, ItemStack card) {
        var frequency = transceiver.getFrequency();
        var owner = transceiver.getPlacer();
        var ownerName = transceiver.getPlacerName();

        card.set(ModDataComponents.DATA_CHANNEL_CARD,
                new DataChannelCard(frequency, owner, ownerName));
    }

    public static void setFrequency(ItemStack stack, long frequency) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);

        DataChannelCard newData;
        if (data != null) newData = new DataChannelCard(frequency, data.owner(), data.ownerName());
        else newData = new DataChannelCard(frequency, null, "");

        stack.set(ModDataComponents.DATA_CHANNEL_CARD, newData);
    }

    public static long getFrequency(ItemStack stack) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);
        return data == null ? 0 : data.frequency();
    }

    public static Pair<@Nullable UUID, String> getOwner(ItemStack stack) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);
        UUID owner = null;
        try {
            owner = data.owner();
        } catch (NullPointerException ignore) {
        }
        return new Pair<>(owner, data == null ? "" : data.ownerName());
    }

    public static void setOwner(ItemStack stack, @Nullable UUID owner, String ownerName) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);
        long frequency = data == null ? 0 : data.frequency();
        stack.set(ModDataComponents.DATA_CHANNEL_CARD,
                new DataChannelCard(frequency, owner, ownerName));
    }

    /**
     * 设置频道卡的所有者UUID
     */
    public static void setOwnerUUID(ItemStack stack, UUID owner) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);

        DataChannelCard newData;
        if (data != null) newData = new DataChannelCard(data.frequency(), owner, data.ownerName());
        else newData = new DataChannelCard(0, owner, "");

        stack.set(ModDataComponents.DATA_CHANNEL_CARD, newData);
    }

    /**
     * 获取频道卡的所有者UUID
     */
    @Nullable
    public static UUID getOwnerUUID(ItemStack stack) {
        try {
            var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);
            return data.owner();
        } catch (NullPointerException ignore) {
            return null;
        }
    }

    // 明明叫TeamName, 又会回退到Name, 这样叫会有歧义的
    /**
     * 设置团队名称（用于显示）
     */
    public static void setOwnerName(ItemStack stack, String teamName) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);

        DataChannelCard newData;
        if (data != null) newData = new DataChannelCard(data.frequency(), data.owner(), teamName);
        else newData = new DataChannelCard(0, null, teamName);

        stack.set(ModDataComponents.DATA_CHANNEL_CARD, newData);
    }

    /**
     * 获取团队名称
     */
    public static String getOwnerName(ItemStack stack) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);
        return data == null ? "" : data.ownerName();
    }

    /**
     * 清除所有者信息
     */
    public static void clearOwner(ItemStack stack) {
        var data = stack.get(ModDataComponents.DATA_CHANNEL_CARD);
        if (data != null) stack.set(ModDataComponents.DATA_CHANNEL_CARD,
                new DataChannelCard(data.frequency(), null, ""));
    }
}
