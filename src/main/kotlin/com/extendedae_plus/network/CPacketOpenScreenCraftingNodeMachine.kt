package com.extendedae_plus.network

import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import com.extendedae_plus.mixin.helper.HelperProviderHost
import com.extendedae_plus.util.UtilMenu
import com.extendedae_plus.util.UtilNetwork
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

/**
 * 客户端从 CraftingCPUScreen 发送：鼠标下条目对应的 AEKey。
 * 服务端在当前打开的 CraftingCPUMenu 所属网络中，定位匹配该 AEKey 的样板供应器，
 * 尝试打开其目标机器的 GUI。
 */
@FishNetworkPacket("open_screen_crafting_node_machine")
@JvmRecord
data class CPacketOpenScreenCraftingNodeMachine(val what: AEKey) : CPacketGeneric {
    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, CPacketOpenScreenCraftingNodeMachine> =
            StreamCodec.composite(
                AEKey.STREAM_CODEC, CPacketOpenScreenCraftingNodeMachine::what,
                ::CPacketOpenScreenCraftingNodeMachine
            )
    }

    override fun handleServer(player: ServerPlayer) {
        val level = player.level()

        UtilNetwork.findProviders(player, this.what)
            .filterIsInstance<HelperProviderHost>()
            .associateWith { it.targets }
            .forEach { (helper, sides) ->
                sides.forEach { side ->
                    val pos = (helper.pos
                        ?.relative(side.opposite))
                        ?: return@forEach

                    val state = level.getBlockState(pos) ?: return@forEach
                    if (helper.targetIcon != AEItemKey.of(state.block.asItem())) return@forEach

                    if (UtilMenu.open(level, pos, side, player)) return
                }
            }
    }
}
