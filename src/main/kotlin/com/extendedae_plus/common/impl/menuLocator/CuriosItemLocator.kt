package com.extendedae_plus.common.impl.menuLocator

import appeng.menu.locator.ItemMenuHostLocator
import com.fish.fishlib.util.FishStreamCodecs
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import top.theillusivec4.curios.api.CuriosApi
import java.util.*

data class CuriosItemLocator(
    val type: String,
    val index: Int,
    val hitResult: BlockHitResult?
) : ItemMenuHostLocator {
    override fun hitResult() = this.hitResult

    override fun locateItem(player: Player) = CuriosApi.getCuriosInventory(player)
        .map { handler ->
            handler.curios[type]
                ?.stacks
                ?.getStackInSlot(index)
        }.orElse(ItemStack.EMPTY)

    override fun toString() = "curiosSlot{$type, $index}"

    companion object {
        val streamCodec: StreamCodec<FriendlyByteBuf, CuriosItemLocator> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CuriosItemLocator::type,
            ByteBufCodecs.INT, CuriosItemLocator::index,
            ByteBufCodecs.optional(FishStreamCodecs.blockHitResult),
            { Optional.ofNullable(it.hitResult) },
            { type, index, resultHit ->
                CuriosItemLocator(type, index, resultHit.orElse(null))
            }
        )
    }
}
