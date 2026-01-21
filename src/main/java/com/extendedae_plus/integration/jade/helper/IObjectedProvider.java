package com.extendedae_plus.integration.jade.helper;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.Accessor;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;

public interface IObjectedProvider<TAccessor extends Accessor<?>> {
    BiConsumer<CompoundTag, TAccessor> getProvider();

    static <TEntry extends Enum<TEntry> & IObjectedProvider<TAccessor>, TAccessor extends Accessor<?>>
    Collection<Pair<String, BiConsumer<CompoundTag, TAccessor>>> getProviders(Class<TEntry> clazzProvider) {
        return Arrays.stream(clazzProvider.getEnumConstants())
                .map(entry -> new Pair<>(entry.name().toLowerCase(), entry.getProvider()))
                .toList();
    }
}