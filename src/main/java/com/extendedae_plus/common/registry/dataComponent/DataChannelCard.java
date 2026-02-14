package com.extendedae_plus.common.registry.dataComponent;

import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.wireless.linkApi.Label;
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

public record DataChannelCard(Label.Data label, @Nullable UUID owner, String ownerName) {
    public static final Codec<DataChannelCard> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Label.Data.CODEC.fieldOf("data_label").forGetter(data -> data.label),
            UUIDUtil.CODEC.lenientOptionalFieldOf("owner")
                    .forGetter(data -> Optional.ofNullable(data.owner())),
            Codec.STRING.fieldOf("owner_name").forGetter(DataChannelCard::ownerName)
    ).apply(inst, (data, owner, ownerName) ->
            new DataChannelCard(data, owner.orElse(null), ownerName)));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataChannelCard> STREAM_CODEC = StreamCodec.composite(
            Label.Data.STREAM_CODEC, data -> data.label,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.owner()),
            ByteBufCodecs.STRING_UTF8, DataChannelCard::ownerName,
            (data, owner, ownerName) ->
                    new DataChannelCard(data, owner.orElse(null), ownerName)
    );

    public static void copyFromTransceiver(BlockEntityWirelessTransceiver transceiver, ItemStack card) {
        var label = transceiver.getLabel().data;
        var owner = transceiver.getPlacer();
        var ownerName = transceiver.getPlacerName();

        card.set(ModDataComponents.CardChannel,
                new DataChannelCard(label, owner, ownerName));
    }

    public static void setLabel(ItemStack stack, Label.Data label, boolean changeOwner) {
        var data = stack.get(ModDataComponents.CardChannel);

        DataChannelCard newData;
        if (data != null)
            newData = new DataChannelCard(label,
                changeOwner ? label.placer() : data.owner(),
                changeOwner ? label.placerName() : data.ownerName());
        else newData = new DataChannelCard(label, label.placer(), label.placerName());

        stack.set(ModDataComponents.CardChannel, newData);
    }

    public static Label.Data getLabel(ItemStack stack) {
        var data = stack.get(ModDataComponents.CardChannel);
        return data == null ? Label.Data.EMPTY : data.label;
    }

    public static Pair<@Nullable UUID, String> getOwner(ItemStack stack) {
        var data = stack.get(ModDataComponents.CardChannel);
        UUID owner = null;
        try {
            owner = data.owner();
        } catch (NullPointerException ignore) {
        }
        return new Pair<>(owner, data == null ? "" : data.ownerName());
    }

    public static void setOwner(ItemStack stack, @Nullable UUID owner, String ownerName) {
        var data = stack.get(ModDataComponents.CardChannel);
        var label = data == null ? Label.Data.EMPTY : data.label();
        stack.set(ModDataComponents.CardChannel,
                new DataChannelCard(label, owner, ownerName));
    }

    /**
     * 设置频道卡的所有者UUID
     */
    public static void setOwnerUUID(ItemStack stack, UUID owner) {
        var data = stack.get(ModDataComponents.CardChannel);

        DataChannelCard newData;
        if (data != null) newData = new DataChannelCard(data.label(), owner, data.ownerName());
        else newData = new DataChannelCard(Label.Data.EMPTY, owner, "");

        stack.set(ModDataComponents.CardChannel, newData);
    }

    /**
     * 获取频道卡的所有者UUID
     */
    @Nullable
    public static UUID getOwnerUUID(ItemStack stack) {
        try {
            var data = stack.get(ModDataComponents.CardChannel);
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
        var data = stack.get(ModDataComponents.CardChannel);

        DataChannelCard newData;
        if (data != null) newData = new DataChannelCard(data.label(), data.owner(), teamName);
        else newData = new DataChannelCard(Label.Data.EMPTY, null, teamName);

        stack.set(ModDataComponents.CardChannel, newData);
    }

    /**
     * 获取团队名称
     */
    public static String getOwnerName(ItemStack stack) {
        var data = stack.get(ModDataComponents.CardChannel);
        return data == null ? "" : data.ownerName();
    }

    /**
     * 清除所有者信息
     */
    public static void clearOwner(ItemStack stack) {
        var data = stack.get(ModDataComponents.CardChannel);
        if (data != null) stack.set(ModDataComponents.CardChannel,
                new DataChannelCard(data.label(), null, ""));
    }
}
