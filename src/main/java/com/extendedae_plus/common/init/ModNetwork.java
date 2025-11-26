package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.network.base.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.objectweb.asm.Type;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class ModNetwork {
    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ExtendedAEPlus.MODID);

        findAnnotatedClasses().forEach(packetClass -> {
            if (BPacketGeneric.class.isAssignableFrom(packetClass))
                registerBiDirectional(registrar, packetClass.asSubclass(BPacketGeneric.class));
            else if (CPacketGeneric.class.isAssignableFrom(packetClass))
                registerClient(registrar, packetClass.asSubclass(CPacketGeneric.class));
            else if (SPacketGeneric.class.isAssignableFrom(packetClass))
                registerServer(registrar, packetClass.asSubclass(SPacketGeneric.class));
        });
    }

    private static Set<Class<?>> findAnnotatedClasses() {
        return ModList.get().getAllScanData().stream()
                .flatMap(scanData -> scanData.getAnnotations().stream())
                .filter(annotationData -> annotationData.annotationType().equals(Type.getType(EAEPNetworkPacket.class)))
                .map(annotationData -> {
                    try {
                        return Class.forName(annotationData.memberName());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Failed to find a PacketGeneric class: ", e);
                    }
                })
                .filter(clazz -> clazz.getPackageName().startsWith("com.extendedae_plus.network"))
                .collect(Collectors.toSet());
    }

    private static <TPacket extends CPacketGeneric> void
    registerClient(PayloadRegistrar registrar, final Class<TPacket> clazzPacket) {
        register(clazzPacket, ((type, streamCodec) ->
                registrar.playToServer(type, streamCodec, CPacketGeneric::handle)));
    }

    private static <TPacket extends SPacketGeneric> void
    registerServer(PayloadRegistrar registrar, final Class<TPacket> clazzPacket) {
        register(clazzPacket, ((type, streamCodec) ->
                registrar.playToClient(type, streamCodec, SPacketGeneric::handle)));
    }

    private static <TPacket extends BPacketGeneric> void
    registerBiDirectional(PayloadRegistrar registrar, final Class<TPacket> clazzPacket) {
        register(clazzPacket, ((type, streamCodec) ->
                registrar.playBidirectional(type, streamCodec, BPacketGeneric::handle)));
    }

    @SuppressWarnings("unchecked")
    private static <TPacket extends PacketGeneric> void
    register(Class<TPacket> clazzPacket,
             BiConsumer<CustomPacketPayload.Type<TPacket>, StreamCodec<RegistryFriendlyByteBuf, TPacket>> registeringAction) {
        try {
            registeringAction.accept((CustomPacketPayload.Type<TPacket>) clazzPacket.getField("TYPE").get(null),
                    (StreamCodec<RegistryFriendlyByteBuf, TPacket>) clazzPacket.getField("STREAM_CODEC").get(null));
        } catch (NoSuchFieldException | ClassCastException | IllegalAccessException ignored) {
        }
    }
}
