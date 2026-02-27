package com.extendedae_plus.common.impl.pattern

import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import com.fish.fishlib.util.extension.optional
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class InfoProvider(
    val name: Component,
    val icon: AEItemKey?,
    val serverID: Int,
    val availableSlots: Int
) {
    fun i18nKey() = this.icon?.id?.toLanguageKey() ?: ""

    companion object {
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, InfoProvider> =
            StreamCodec.composite(
                ComponentSerialization.TRUSTED_STREAM_CODEC, InfoProvider::name,
                ByteBufCodecs.optional(AEKey.STREAM_CODEC), InfoProvider::icon.optional(),
                ByteBufCodecs.INT, InfoProvider::serverID,
                ByteBufCodecs.INT, InfoProvider::availableSlots,
                { name, icon, id, slots ->
                    InfoProvider(
                        name,
                        icon.getOrNull() as? AEItemKey,
                        id,
                        slots
                    )
                }
            )
    }
}
