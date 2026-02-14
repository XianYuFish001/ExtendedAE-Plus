package com.extendedae_plus.network.base

import com.extendedae_plus.ExtendedAEPlus
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModList
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadHandler
import net.neoforged.neoforgespi.language.ModFileScanData
import org.objectweb.asm.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.staticProperties

@Suppress("unchecked_cast")
@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
object Initializer {
    internal val Types = HashMap<String, CustomPacketPayload.Type<out PacketGeneric>>()

    @SubscribeEvent
    private fun init(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(ExtendedAEPlus.MODID)
        ModList.get().allScanData
            .flatMap(ModFileScanData::getAnnotations)
            .filter { Type.getType(EAEPNetworkPacket::class.java) == it.annotationType }
            .map { it.annotationData["value"] as String to Class.forName(it.memberName).kotlin }
            .forEach { (type, clazz) ->
                this.Types.putIfAbsent(
                    clazz.simpleName ?: return@forEach,
                    CustomPacketPayload.Type(ExtendedAEPlus.getLocation(type))
                )
                when (clazz::isSubclassOf) {
                    BPacketGeneric::class -> this.register(
                        clazz as KClass<BPacketGeneric>,
                        registrar::playBidirectional,
                        BPacketGeneric::handle
                    )

                    CPacketGeneric::class -> this.register(
                        clazz as KClass<CPacketGeneric>,
                        registrar::playToServer,
                        CPacketGeneric::handle
                    )

                    SPacketGeneric::class -> this.register(
                        clazz as KClass<SPacketGeneric>,
                        registrar::playToClient,
                        SPacketGeneric::handle
                    )
                }
            }
    }

    private fun <TPacket : PacketGeneric> register(
        clazz: KClass<TPacket>,
        register: (
            CustomPacketPayload.Type<TPacket>,
            StreamCodec<RegistryFriendlyByteBuf, TPacket>,
            IPayloadHandler<TPacket>
        ) -> Unit,
        handler: IPayloadHandler<TPacket>
    ) = register(
        this.Types[clazz.simpleName]
                as? CustomPacketPayload.Type<TPacket>
            ?: throw IllegalStateException("Found EAEPPacket ${clazz.simpleName} with UNKNOWN type"),
        clazz.staticProperties
            .filterIsInstance<KProperty0<StreamCodec<*, *>>>()
            .ifEmpty { throw IllegalStateException("Found EAEPPacket ${clazz.simpleName} with NO StreamCodec") }[0]
            .get() as StreamCodec<RegistryFriendlyByteBuf, TPacket>,
        handler
    )
}