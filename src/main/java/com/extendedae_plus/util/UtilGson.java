package com.extendedae_plus.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.slf4j.Logger;

import java.io.IOException;

public class UtilGson {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();

    public static final TypeAdapter<Component> adapterComponent = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Component value) throws IOException {
            if (value.getString().isBlank()) {
                out.nullValue();
                return;
            }

            var encoded = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, value)
                    .resultOrPartial(error -> LOGGER.error("Failed to encode Components: {}", error));
            if (encoded.isEmpty()) {
                out.nullValue();
                return;
            }

            out.jsonValue(GSON.toJson(encoded.get()));
        }

        @Override
        public Component read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(in))
                    .resultOrPartial(error -> LOGGER.error("Failed to decode Components: {}", error))
                    .map(Pair::getFirst)
                    .orElse(Component.empty());
        }
    };
}
