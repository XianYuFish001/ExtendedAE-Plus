package com.extendedae_plus.common.registry.item.priorityTool

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.StringRepresentable
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs

@JvmRecord
data class DataPriority(val priority: Int, val modeTool: ModeTool) {
    fun apply(): DataPriority {
        val valueToApply = when (this.modeTool) {
            ModeTool.Keep -> 0
            ModeTool.Decrement -> -1
            ModeTool.Increment -> 1
        }
        return DataPriority(this.priority + valueToApply, this.modeTool)
    }

    enum class ModeTool : StringRepresentable {
        Keep, Increment, Decrement;

        override fun getSerializedName() = this.name.lowercase()
    }

    companion object {
        val codec: Codec<DataPriority> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("priority").forGetter(DataPriority::priority),
                StringRepresentable.fromValues(ModeTool::values)
                    .fieldOf("mode_tool").forGetter(DataPriority::modeTool)
            ).apply(instance, ::DataPriority)
        }

        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, DataPriority> = StreamCodec.composite(
            ByteBufCodecs.INT,
            DataPriority::priority,
            NeoForgeStreamCodecs.enumCodec(ModeTool::class.java),
            DataPriority::modeTool,
            ::DataPriority
        )
    }
}
