package com.extendedae_plus.common.wireless.linkApi

import com.extendedae_plus.common.impl.guiSync.PacketStreamable
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationFTBTeams
import com.fish.fishlib.util.extension.optional
import com.fish.fishlib.util.extension.predicated
import com.fish.fishlib.util.oneOf.OneOf2
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.lang.ref.WeakReference
import java.util.*
import kotlin.jvm.optionals.getOrNull

class Label internal constructor(val data: Data) {
    internal var master = WeakReference<ILinkHost?>(null)
    internal val listeners = HashSet<WeakReference<ILinkListener?>>()

    override fun equals(other: Any?) = this.data == other

    override fun hashCode() = Objects.hash(this.data, this.master, this.listeners)

    @JvmRecord
    data class Data(
        val frequency: Long?,
        val label: String,
        val placer: UUID?,
        val placerName: String,
        private val desc: Component
    ) : PacketStreamable {
        fun pack() = RegistryLink.registerOrGetLabel(this)

        fun convertPlacer() = ManagerIntegration<IntegrationFTBTeams>()
            ?.getTeamUUID(this.placer)?.let {
                Data(this.frequency, this.label, it, this.placerName, this.desc)
            } ?: this

        val displayValue: String
            get() {
                return if (this.isEmpty) ""
                else if (this.frequency != null) this.frequency.toString()
                else this.label
            }

        val isEmpty: Boolean
            get() = this.frequency == null && this.label.isBlank()

        fun description(): Component =
            if (this.isEmpty)
                Component.empty()
            else this.desc

        fun wrapLabel(): OneOf2<Long, String> =
            if (this.label.isBlank())
                OneOf2.a(this.frequency)
            else OneOf2.b(this.label)

        override fun equals(other: Any?) = when (other) {
            is Data -> {
                if (this.placer != other.placer) false
                else if (other.frequency == null)
                    (this.label == other.label)
                else (this.frequency == other.frequency)
            }

            is Label -> (this == other.data)
            is Long -> (this.frequency == other)
            is String -> (this.label == other)
            else -> false
        }

        override fun hashCode() =
            if (frequency == null)
                Objects.hashCode(label)
            else Objects.hashCode(frequency)

        override fun toString(): String {
            var value = ""
            var description = ", description=" + this.desc.string
            if (this.isEmpty) {
                value = "empty"
                description = ""
            } else if (this.frequency != null) {
                value = "frequency="
                value += this.frequency
            } else if (!this.label.isBlank()) {
                value = "label="
                value += this.label
            }

            return "Label.Data{$value$description}"
        }

        companion object {
            @JvmField
            val codec: Codec<Data> = RecordCodecBuilder.create {
                it.group(
                    OneOf2.mapCodec("value", Codec.LONG, Codec.STRING).forGetter(Data::wrapLabel),
                    UUIDUtil.CODEC.lenientOptionalFieldOf("placer").forGetter(Data::placer.optional()),
                    Codec.STRING.fieldOf("placer_name").forGetter(Data::placerName),
                    ComponentSerialization.CODEC.lenientOptionalFieldOf("description")
                        .forGetter(Data::description.optional())
                ).apply(it) { value, placer, placerName, description ->
                    Data(
                        value.a,
                        value.b ?: "",
                        placer.getOrNull(),
                        placerName,
                        description.orElse(Component.empty())
                    )
                }
            }.predicated(Data::isEmpty, ::Empty)

            @JvmField
            val streamCodec: StreamCodec<RegistryFriendlyByteBuf, Data> = StreamCodec.composite(
                OneOf2.streamCodec(ByteBufCodecs.VAR_LONG, ByteBufCodecs.STRING_UTF8), Data::wrapLabel,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), Data::placer.optional(),
                ByteBufCodecs.STRING_UTF8, Data::placerName,
                ComponentSerialization.OPTIONAL_STREAM_CODEC, Data::description.optional()
            ) { value, placer, placerName, description ->
                Data(
                    value.a,
                    value.b ?: "",
                    placer.getOrNull(),
                    placerName,
                    description.orElse(Component.empty())
                )
            }.predicated(Data::isEmpty, ::Empty)

            @JvmField
            val Empty = Data(null, "", null, "", Component.empty())

            @JvmStatic
            fun of(frequency: Long, placer: UUID?, description: Component) = Data(
                frequency,
                "",
                placer,
                ManagerIntegration<IntegrationFTBTeams>()
                    ?.getTeamName(placer)
                    ?.string ?: "",
                description
            )

            @JvmStatic
            fun of(label: String, placer: UUID?, description: Component) = Data(
                null,
                label,
                placer,
                ManagerIntegration<IntegrationFTBTeams>()
                    ?.getTeamName(placer)
                    ?.string ?: "",
                description
            )

            init {
                PacketStreamable.register(Data::class, streamCodec)
            }
        }
    }

    companion object {
        @JvmField
        val Empty = Label(Data.Empty)
    }
}
