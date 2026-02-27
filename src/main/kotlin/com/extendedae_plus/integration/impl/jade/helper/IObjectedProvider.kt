package com.extendedae_plus.integration.impl.jade.helper

import com.mojang.datafixers.util.Pair
import net.minecraft.nbt.CompoundTag
import snownee.jade.api.Accessor

internal typealias Provider<T> = MutableCollection<Pair<String, (CompoundTag, T) -> Unit>>

interface IObjectedProvider<TAccessor : Accessor<*>> {
    val provider: (CompoundTag, TAccessor) -> Unit

    companion object {
        fun <TEntry, TAccessor : Accessor<*>> getProviders(
            clazzProvider: Class<TEntry>
        ): Provider<TAccessor>
                where TEntry : Enum<TEntry>,
                      TEntry : IObjectedProvider<TAccessor> =
            clazzProvider.getEnumConstants()
                .mapTo(ArrayList()) { Pair(it.name.lowercase(), it.provider) }
    }
}