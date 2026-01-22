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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class ModNetwork {
    private static final Map<String, CustomPacketPayload.Type<? extends PacketGeneric>> types = new HashMap<>();

    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ExtendedAEPlus.MODID);

        findAnnotatedClasses().forEach((typePacket, clazzPacket) -> {
            types.putIfAbsent(clazzPacket.getSimpleName(), new CustomPacketPayload.Type<>(ExtendedAEPlus.getLocation(typePacket)));
            if (BPacketGeneric.class.isAssignableFrom(clazzPacket))
                registerBiDirectional(registrar, clazzPacket.asSubclass(BPacketGeneric.class));
            else if (CPacketGeneric.class.isAssignableFrom(clazzPacket))
                registerClient(registrar, clazzPacket.asSubclass(CPacketGeneric.class));
            else if (SPacketGeneric.class.isAssignableFrom(clazzPacket))
                registerServer(registrar, clazzPacket.asSubclass(SPacketGeneric.class));
        });
    }

    public static CustomPacketPayload.Type<? extends PacketGeneric> getType(String name) {
        return types.get(name);
    }

    private static Map<String, Class<?>> findAnnotatedClasses() {
        return ModList.get().getAllScanData().stream()
                .flatMap(scanData -> scanData.getAnnotations().stream())
                .filter(data -> data.annotationType().equals(Type.getType(EAEPNetworkPacket.class)))
                .collect(Collectors.toMap(data -> (String) data.annotationData().get("value"), data -> {
                    try {
                        return Class.forName(data.memberName());
                    } catch (ClassNotFoundException exception) {
                        throw new IllegalStateException("Failed to find Packet: " + data.memberName() + ", ", exception);
                    }
                }));
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
            registeringAction.accept((CustomPacketPayload.Type<TPacket>) types.get(clazzPacket.getSimpleName()),
                    (StreamCodec<RegistryFriendlyByteBuf, TPacket>) clazzPacket.getField("STREAM_CODEC").get(null));
        } catch (ClassCastException | IllegalAccessException exception) {
            throw new IllegalStateException("Failed to register Packet: " + clazzPacket.getSimpleName() + ", ", exception);
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("Failed to find STREAM_CODEC in " + clazzPacket.getSimpleName(), exception);
        }
    }
}
