package com.extendedae_plus.integration.impl.jade.helper

import com.extendedae_plus.ExtendedAEPlus
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.Accessor
import snownee.jade.api.IServerDataProvider

@JvmRecord
data class WrapperObjectedProvider<TAccessor : Accessor<*>>(
    val uid: ResourceLocation,
    val providers: Provider<TAccessor>
) : IServerDataProvider<TAccessor> {
    override fun appendServerData(serverData: CompoundTag, accessor: TAccessor) {
        this.providers.forEach { provider ->
            val data = CompoundTag()
            provider.second(data, accessor)
            serverData.put(provider.getFirst(), data)
        }
    }

    override fun getUid(): ResourceLocation {
        return this.uid
    }

    companion object {
        fun <TAccessor : Accessor<*>> create(
            uid: String,
            providers: Provider<TAccessor>
        ) = WrapperObjectedProvider(ExtendedAEPlus.getLocation(uid), providers)

        fun <TAccessor : Accessor<*>, TProvider> create(
            uid: String,
            clazzProvider: Class<TProvider>
        ) where TProvider : Enum<TProvider>,
                TProvider : IObjectedProvider<TAccessor> =
            create(uid, IObjectedProvider.getProviders(clazzProvider))
    }
}
