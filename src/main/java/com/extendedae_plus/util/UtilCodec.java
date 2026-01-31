package com.extendedae_plus.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class UtilCodec {
    public static <TBuffer extends ByteBuf, TValve> StreamCodec<TBuffer, TValve> streamCodecPredicated(
            Predicate<TValve> predicate, Supplier<TValve> unit, StreamCodec<TBuffer, TValve> codec
    ) {
        return StreamCodec.of((buffer, value) -> {
            var tested = predicate.test(value);
            buffer.writeBoolean(tested);

            if (tested) return;
            codec.encode(buffer, value);
        }, buffer -> {
            if (buffer.readBoolean())
                return unit.get();
            else return codec.decode(buffer);
        });
    }

    public static <TValue> Codec<TValue> codecPredicated(Predicate<TValue> predicate, Supplier<TValue> unit, Codec<TValue> codec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<TValue, T>> decode(DynamicOps<T> ops, T input) {
                var predicated = ops.getStringValue(input).map("predicated_empty"::equals);
                if (predicated.isSuccess() && predicated.getOrThrow())
                    return DataResult.success(new Pair<>(unit.get(), input));
                return codec.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(TValue input, DynamicOps<T> ops, T prefix) {
                if (predicate.test(input))
                    return DataResult.success(ops.createString("predicated_empty"));
                return codec.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "CodecPredicated[" + codec + "]";
            }
        };
        // 这么写有点丑陋(
//        return new OptionalFieldCodec<>("predicated", codec, false)
//                .xmap(value -> value.orElse(unit.get()),
//                        value -> predicate.test(value) ? Optional.empty() : Optional.of(value))
//                .codec();
    }

    public static <TBuffer, TValue> BiConsumer<TValue, TBuffer> encodeReversed(StreamCodec<TBuffer, TValue> codec) {
        return (value, buffer) -> codec.encode(buffer, value);
    }

    public static class StreamCodecs {
        public static final StreamCodec<FriendlyByteBuf, BlockHitResult> blockHitResult =
                StreamCodec.of(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult);
    }
}
