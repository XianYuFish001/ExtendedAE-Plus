package com.extendedae_plus.integration.jade;

import com.extendedae_plus.ExtendedAEPlus;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.IServerDataProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

public record ObjectedProvider<TAccessor extends Accessor<?>>
        (ResourceLocation uid, Collection<Pair<String, BiConsumer<CompoundTag, TAccessor>>> providers)
        implements IServerDataProvider<TAccessor> {
    public static <TAccessor extends Accessor<?>> ObjectedProvider<TAccessor>
    create(String uid, Collection<Pair<String, BiConsumer<CompoundTag, TAccessor>>> providers) {
        return new ObjectedProvider<>(ExtendedAEPlus.getLocation(uid), providers);
    }

    @Override
    public void appendServerData(CompoundTag serverData, TAccessor accessor) {
        this.providers.forEach(provider -> {
            var data = new CompoundTag();
            provider.getSecond().accept(data, accessor);
            serverData.put(provider.getFirst(), data);
        });
    }

    @Override
    public ResourceLocation getUid() {
        return this.uid;
    }

    public interface IObjectedProvider<TAccessor extends Accessor<?>> {
        BiConsumer<CompoundTag, TAccessor> getProvider();

        static <TEntry extends Enum<TEntry> & IObjectedProvider<TAccessor>, TAccessor extends Accessor<?>>
        Collection<Pair<String, BiConsumer<CompoundTag, TAccessor>>> getProviders(Class<TEntry> clazzProvider) {
            var entries = clazzProvider.getEnumConstants();
            var providers = new ArrayList<Pair<String, BiConsumer<CompoundTag, TAccessor>>>();

            for (var entry : entries)
                providers.add(new Pair<>(entry.name().toLowerCase(), entry.getProvider()));

            return providers;
        }
    }
}
