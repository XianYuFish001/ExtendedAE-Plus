package com.extendedae_plus.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

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
}
