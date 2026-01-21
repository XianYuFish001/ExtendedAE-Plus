package com.extendedae_plus.integration.jade.helper;

import com.extendedae_plus.ExtendedAEPlus;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.IServerDataProvider;

import java.util.Collection;
import java.util.function.BiConsumer;

public record WrapperObjectedProvider<TAccessor extends Accessor<?>>(
        ResourceLocation uid,
        Collection<Pair<String, BiConsumer<CompoundTag, TAccessor>>> providers
) implements IServerDataProvider<TAccessor> {
    public static <TAccessor extends Accessor<?>> WrapperObjectedProvider<TAccessor>
    create(String uid, Collection<Pair<String, BiConsumer<CompoundTag, TAccessor>>> providers) {
        return new WrapperObjectedProvider<>(ExtendedAEPlus.getLocation(uid), providers);
    }

    public static <TAccessor extends Accessor<?>, TProvider extends Enum<TProvider> & IObjectedProvider<TAccessor>>
    WrapperObjectedProvider<TAccessor> create(String uid, Class<TProvider> clazzProvider) {
        return create(uid, IObjectedProvider.getProviders(clazzProvider));
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

}
