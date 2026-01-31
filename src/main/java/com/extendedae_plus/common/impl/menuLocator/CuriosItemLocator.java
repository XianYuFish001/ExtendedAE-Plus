package com.extendedae_plus.common.impl.menuLocator;

import appeng.menu.locator.ItemMenuHostLocator;
import com.extendedae_plus.util.UtilCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public record CuriosItemLocator(String type, int index, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    public static final StreamCodec<FriendlyByteBuf, CuriosItemLocator> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CuriosItemLocator::type,
            ByteBufCodecs.INT, CuriosItemLocator::index,
            ByteBufCodecs.optional(UtilCodec.StreamCodecs.blockHitResult),
            data -> Optional.ofNullable(data.hitResult),
            (type, index, resultHit) ->
                    new CuriosItemLocator(type, index, resultHit.orElse(null))
    );

    @Override
    public ItemStack locateItem(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler ->
                        handler.getCurios()
                                .get(type)
                                .getStacks()
                                .getStackInSlot(index))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public String toString() {
        return "curiosSlot{" + type + "," + index + "}";
    }
}
