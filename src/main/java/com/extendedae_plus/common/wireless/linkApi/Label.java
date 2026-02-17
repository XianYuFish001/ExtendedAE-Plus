package com.extendedae_plus.common.wireless.linkApi;

import com.extendedae_plus.common.impl.guiSync.PacketStreamable;
import com.extendedae_plus.integration.IntegrationFTBTeams;
import com.extendedae_plus.util.UtilCodec;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class Label {
    public static final Label EMPTY = new Label(Data.EMPTY);

    public final Data data;
    WeakReference<@Nullable ILinkHost> master = new WeakReference<>(null);
    final Set<WeakReference<@Nullable ILinkListener>> listeners = new HashSet<>();

    Label(Data data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        return this.data.equals(obj);
    }

    public record Data(
            @Nullable Long frequency,
            String label,
            @Nullable UUID placer,
            String placerName,
            Component description
    ) implements PacketStreamable {
        public static final Codec<Data> CODEC = UtilCodec.codecPredicated(
                Data::isEmpty, () -> Data.EMPTY, RecordCodecBuilder.create(inst -> inst.group(
                Codec.either(Codec.LONG, Codec.STRING).fieldOf("value").forGetter(Data::toEither),
                UUIDUtil.CODEC.lenientOptionalFieldOf("placer").forGetter(data -> Optional.ofNullable(data.placer)),
                Codec.STRING.fieldOf("placer_name").forGetter(Data::placerName),
                ComponentSerialization.CODEC.lenientOptionalFieldOf("description")
                        .forGetter(data -> Optional.ofNullable(data.description))
        ).apply(inst, (value, placer, placerName, description) ->
                new Data(value.left().orElse(null),
                        value.right().orElse(""),
                        placer.orElse(null),
                        placerName,
                        description.orElse(Component.empty())))));

        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = UtilCodec.streamCodecPredicated(
                Data::isEmpty, () -> Data.EMPTY, StreamCodec.composite(
                        ByteBufCodecs.either(ByteBufCodecs.VAR_LONG, ByteBufCodecs.STRING_UTF8), Data::toEither,
                        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.placer),
                        ByteBufCodecs.STRING_UTF8, Data::placerName,
                        ComponentSerialization.OPTIONAL_STREAM_CODEC, data -> Optional.ofNullable(data.description),
                        (value, placer, placerName, description) ->
                                new Data(value.left().orElse(null),
                                        value.right().orElse(""),
                                        placer.orElse(null),
                                        placerName,
                                        description.orElse(Component.empty()))));

        public static final Data EMPTY = new Data(null, "", null, "", Component.empty());

        public Label pack() {
            return RegistryLink.registerOrGetLabel(this);
        }

        public static Data of(long frequency, @Nullable UUID placer, Component description) {
            return new Data(frequency, "", placer, IntegrationFTBTeams.instance.getTeamName(placer).getString(), description);
        }

        public static Data of(String label, @Nullable UUID placer, Component description) {
            return new Data(null, label, placer, IntegrationFTBTeams.instance.getTeamName(placer).getString(), description);
        }

        public String getDisplayValue() {
            if (this.isEmpty()) return "";
            else if (this.frequency != null) return this.frequency.toString();
            else return this.label;
        }

        public boolean isEmpty() {
            return this.frequency == null && this.label.isBlank();
        }

        @Override
        public Component description() {
            if (this.isEmpty()) return Component.empty();
            else return this.description;
        }

        public Either<@Nullable Long, String> toEither() {
            if (this.label.isBlank()) return Either.left(this.frequency);
            else return Either.right(this.label);
        }

        public Data convertPlacer() {
            return IntegrationFTBTeams.instance
                    .getTeamUUID(this.placer)
                    .map(uuid -> new Data(this.frequency, this.label, uuid, this.placerName, this.description))
                    .orElse(this);
        }

        static {
            PacketStreamable.register(Label.Data.class, STREAM_CODEC);
        }

        @Override
        public boolean equals(Object o) {
            return switch (o) {
                case Data data -> {
                    if (!Objects.equals(this.placer, data.placer)) yield false;
                    if (data.frequency == null)
                        yield Objects.equals(this.label, data.label);
                    else yield Objects.equals(this.frequency, data.frequency);
                }
                case Label label -> this.equals(label.data);
                case Long frequency -> Objects.equals(this.frequency, frequency);
                case String label -> Objects.equals(this.label, label);
                case null, default -> false;
            };
        }

        @Override
        public int hashCode() {
            return frequency == null
                    ? Objects.hashCode(label)
                    : Objects.hashCode(frequency);
        }

        @Override
        public String toString() {
            var value = "";
            var description = ", description=" + this.description.getString();
            if (this.isEmpty()) {
                value = "empty";
                description = "";
            } else if (this.frequency != null) {
                value = "frequency=";
                value += this.frequency;
            } else if (!this.label.isBlank()) {
                value = "label=";
                value += this.label;
            }

            return "LabelLink.Data{" + value + description + '}';
        }
    }
}
