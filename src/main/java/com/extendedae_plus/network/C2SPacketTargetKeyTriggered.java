package com.extendedae_plus.network;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.client.HelperCtrlKeyPress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SPacketTargetKeyTriggered(KeyType keyType) {
    public enum KeyType {
        CTRL_DOWN,
        CTRL_UP
    }

    public static void encode(C2SPacketTargetKeyTriggered msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.keyType);
    }

    public static C2SPacketTargetKeyTriggered decode(FriendlyByteBuf buf) {
        KeyType keyType = buf.readEnum(KeyType.class);
        return new C2SPacketTargetKeyTriggered(keyType);
    }

    public static void handle(C2SPacketTargetKeyTriggered packet, Supplier<NetworkEvent.Context> sup) {
        var context = sup.get();
        context.enqueueWork(() -> {
            if (context.getSender().level().isClientSide) return;
            ServerPlayer player = context.getSender();

            if (player.containerMenu instanceof PatternEncodingTermMenu patternMenu) {
                if (patternMenu instanceof HelperCtrlKeyPress helper) {
                    helper.eaep$setPressed(switch (packet.keyType) {
                        case CTRL_UP -> false;
                        case CTRL_DOWN -> true;
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }
}
