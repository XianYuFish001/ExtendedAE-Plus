package com.extendedae_plus.common.dataComponent;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record DataEncoderProfile(String name, @Nullable UUID uuid) {
    public static final Codec<DataEncoderProfile> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(DataEncoderProfile::name),
            UUIDUtil.CODEC.lenientOptionalFieldOf("uuid").forGetter(data -> Optional.ofNullable(data.uuid))
    ).apply(inst, (name, uuid) -> new DataEncoderProfile(name, uuid.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataEncoderProfile> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DataEncoderProfile::name,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.uuid),
            (name, uuid) -> new DataEncoderProfile(name, uuid.orElse(null))
    );

    public DataEncoderProfile(GameProfile profile) {
        this(profile.getName(), profile.getId());
    }
}
