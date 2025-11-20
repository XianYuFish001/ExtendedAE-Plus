package com.extendedae_plus.mixin.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DataProviderSettings(boolean smartDoubling, boolean smartBlocking) {
    public static final Codec<DataProviderSettings> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("smart_doubling").forGetter(DataProviderSettings::smartDoubling),
            Codec.BOOL.fieldOf("smart_blocking").forGetter(DataProviderSettings::smartBlocking)
    ).apply(inst, DataProviderSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataProviderSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DataProviderSettings::smartDoubling,
            ByteBufCodecs.BOOL, DataProviderSettings::smartBlocking,
            DataProviderSettings::new
    );
}
