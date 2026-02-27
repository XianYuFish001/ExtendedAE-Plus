package com.extendedae_plus.common.registry.dataComponent

import com.fish.fishlib.util.extension.optional
import com.mojang.authlib.GameProfile
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.*
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class DataEncoderProfile(val name: String, val uuid: UUID?) {
    constructor(profile: GameProfile) : this(profile.name, profile.id)

    companion object {
        val codec: Codec<DataEncoderProfile> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("name").forGetter(DataEncoderProfile::name),
                UUIDUtil.CODEC.lenientOptionalFieldOf("uuid").forGetter(DataEncoderProfile::uuid.optional())
            ).apply(instance) { name, uuid ->
                DataEncoderProfile(name, uuid.getOrNull())
            }
        }

        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, DataEncoderProfile> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DataEncoderProfile::name,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), DataEncoderProfile::uuid.optional()
        ) { name, uuid -> DataEncoderProfile(name, uuid.getOrNull()) }
    }
}
