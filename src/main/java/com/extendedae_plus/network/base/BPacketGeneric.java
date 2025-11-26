package com.extendedae_plus.network.base;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface BPacketGeneric extends CPacketGeneric, SPacketGeneric {
    @Override
    default void handle(final IPayloadContext context) {
        switch (context.flow()) {
            case CLIENTBOUND -> CPacketGeneric.super.handle(context);
            case SERVERBOUND -> SPacketGeneric.super.handle(context);
        }
    }
}
