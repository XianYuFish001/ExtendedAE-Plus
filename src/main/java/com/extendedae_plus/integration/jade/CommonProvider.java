package com.extendedae_plus.integration.jade;

import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.IServerDataProvider;

import java.util.Collection;
import java.util.function.BiConsumer;

public record CommonProvider<TAccessor extends Accessor<?>>
        (ResourceLocation uid, Collection<BiConsumer<CompoundTag, TAccessor>> providers)
        implements IServerDataProvider<TAccessor> {
    public static <TAccessor extends Accessor<?>> CommonProvider<TAccessor>
    create(String uid, Collection<BiConsumer<CompoundTag, TAccessor>> providers) {
        return new CommonProvider<>(ExtendedAEPlus.getLocation(uid), providers);
    }

    @Override
    public void appendServerData(CompoundTag data, TAccessor accessor) {
        this.providers.forEach(provider -> provider.accept(data, accessor));
    }

    @Override
    public ResourceLocation getUid() {
        return this.uid;
    }
}
