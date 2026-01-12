package com.extendedae_plus.common.registry.item.priorityTool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record DataPriority(int priority, ModeTool modeTool) {
    public static final Codec<DataPriority> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("priority").forGetter(DataPriority::priority),
            StringRepresentable.fromValues(ModeTool::values).fieldOf("mode_tool").forGetter(DataPriority::modeTool)
    ).apply(inst, DataPriority::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPriority> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DataPriority::priority,
            NeoForgeStreamCodecs.enumCodec(ModeTool.class), DataPriority::modeTool,
            DataPriority::new
    );

    public DataPriority apply() {
        var valueToApply = switch (this.modeTool) {
            case KEEP -> 0;
            case DECREMENT -> -1;
            case INCREMENT -> 1;
        };
        return new DataPriority(this.priority + valueToApply, this.modeTool);
    }

    public enum ModeTool implements StringRepresentable {
        KEEP, INCREMENT, DECREMENT;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }
}
