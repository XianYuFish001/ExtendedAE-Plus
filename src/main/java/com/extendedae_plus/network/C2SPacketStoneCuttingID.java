package com.extendedae_plus.network;

import appeng.menu.me.items.PatternEncodingTermMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SPacketStoneCuttingID(ResourceLocation recipeID) {
    public static void encode(C2SPacketStoneCuttingID packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.recipeID);
    }

    public static C2SPacketStoneCuttingID decode(FriendlyByteBuf buf) {
        ResourceLocation location = buf.readResourceLocation();
        return new C2SPacketStoneCuttingID(location);
    }

    public static void handle(C2SPacketStoneCuttingID packet, Supplier<NetworkEvent.Context> sup) {
        var context = sup.get();
        if (context.getSender().level().isClientSide) return;
        context.enqueueWork(() -> {
            if (!(context.getSender().containerMenu instanceof PatternEncodingTermMenu menu)) return;
            menu.setStonecuttingRecipeId(packet.recipeID);
        });
        context.setPacketHandled(true);
    }
}
