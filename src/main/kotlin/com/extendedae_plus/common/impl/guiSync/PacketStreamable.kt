package com.extendedae_plus.common.impl.guiSync

import appeng.menu.guisync.PacketWritable
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import kotlin.reflect.KClass

/**
 * @see com.extendedae_plus.mixin.core.ae2.MixinSyncedField.MixinFieldCustom
 */
interface PacketStreamable : PacketWritable {
    override fun writeToPacket(data: RegistryFriendlyByteBuf) =
        getStreamCodec(this::class)?.encode(data, this) ?: Unit

    companion object {
        fun <TField : PacketStreamable> register(
            clazzField: KClass<TField>,
            streamCodec: StreamCodec<RegistryFriendlyByteBuf, TField>
        ) = check(
            registry.put(
                clazzField,
                streamCodec
            ) == null
        ) { "Duplicate field found for " + clazzField.simpleName }

        @JvmStatic
        @Suppress("unchecked_cast")
        fun getStreamCodec(clazzField: KClass<*>): StreamCodec<RegistryFriendlyByteBuf, Any>? {
            val streamCodec = registry[clazzField]
            check(!(PacketStreamable::class.java.isAssignableFrom(clazzField.java) && streamCodec == null)) {
                "Unregistered streamable object: " + clazzField.simpleName
            }
            return streamCodec as? StreamCodec<RegistryFriendlyByteBuf, Any>
        }

        val registry = HashMap<KClass<out PacketStreamable>, StreamCodec<RegistryFriendlyByteBuf, out PacketStreamable>>()
    }
}
