package com.extendedae_plus.util;

import appeng.menu.locator.ItemMenuHostLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public record CuriosItemLocator(String curioType, int curioIndex,
                                @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    @Override
    public ItemStack locateItem(Player player) {
        var curiosInv = CuriosApi.getCuriosInventory(player);
        return curiosInv.map(handler -> handler.getCurios().get(curioType)
                .getStacks().getStackInSlot(curioIndex)).orElse(ItemStack.EMPTY);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeUtf(curioType);
        buf.writeInt(curioIndex);
        buf.writeOptional(Optional.ofNullable(hitResult), FriendlyByteBuf::writeBlockHitResult);
    }

    public static CuriosItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CuriosItemLocator(
                buf.readUtf(),
                buf.readInt(),
                buf.readOptional(FriendlyByteBuf::readBlockHitResult).orElse(null));
    }

    @Override
    public String toString() {
        return "curiosSlot{" + curioType + "," + curioIndex + "}";
    }
}
