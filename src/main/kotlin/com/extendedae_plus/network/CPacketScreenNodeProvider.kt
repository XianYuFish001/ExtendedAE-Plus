package com.extendedae_plus.network

import appeng.api.stacks.AEKey
import com.extendedae_plus.mixin.helper.HelperProviderHost
import com.extendedae_plus.util.UtilKeyBuilder
import com.extendedae_plus.util.UtilNetwork
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToPlayer
import com.fish.fishlib.util.extension.findInstance
import com.fish.fishlib.util.extension.unit
import com.fish.fishlib.util.keyBuilder.Patterns
import com.glodblock.github.extendedae.client.render.EAEHighlightHandler
import com.glodblock.github.extendedae.util.FCClientUtil
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB

/**
 * 客户端从 CraftingCPUScreen 发送：鼠标下条目对应的 AEKey。
 * 服务端在当前打开的 CraftingCPUMenu 所属网络中，定位匹配该 AEKey 的样板供应器，
 * 打开该供应器自身的 UI（不是目标机器的 UI）。
 */
@FishNetworkPacket("open_screen_crafting_node_provider")
@JvmRecord
data class CPacketScreenNodeProvider(val key: AEKey) : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) = UtilNetwork.findProviders(player, this.key)
        .findInstance<HelperProviderHost>()
        ?.also { highlight(it, player) }
        ?.also { it.opener(player, null) }
        ?.let { SPacketHighlightPatternSlot(this.key).sendToPlayer(player) }
        .unit()

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, CPacketScreenNodeProvider> =
            StreamCodec.composite(
                AEKey.STREAM_CODEC, CPacketScreenNodeProvider::key,
                ::CPacketScreenNodeProvider
            )

        private fun highlight(
            host: HelperProviderHost,
            player: Player
        ) {
            val pos = host.pos ?: return
            val dim = host.level?.dimension() ?: return
            val face = host.side

            player.displayClientMessage(
                UtilKeyBuilder.of(Patterns.Message)
                    .addStr("opened_provider_info")
                    .args(pos.toShortString(), dim.location().path)
                    .build(),
                false
            )

            val endTime = System.currentTimeMillis() + 6000
//                    (6000 * Math.clamp(multiplier, 1.0, 30.0)).toLong()

            if (face == null) {
                EAEHighlightHandler.highlight(pos, dim, endTime)
                return
            }

            var origin = AABB(
                2 / 16.0,
                2 / 16.0,
                0.0,
                14 / 16.0,
                14 / 16.0,
                2 / 16.0
            ).move(pos)
            val center = AABB(pos).center
            when (face) {
                Direction.WEST -> origin =
                    FCClientUtil.rotor(origin, center, Direction.Axis.Y, (Math.PI / 2).toFloat())

                Direction.SOUTH -> origin =
                    FCClientUtil.rotor(origin, center, Direction.Axis.Y, Math.PI.toFloat())

                Direction.EAST -> origin =
                    FCClientUtil.rotor(origin, center, Direction.Axis.Y, (-Math.PI / 2).toFloat())

                Direction.UP -> origin =
                    FCClientUtil.rotor(origin, center, Direction.Axis.X, (-Math.PI / 2).toFloat())

                Direction.DOWN -> origin =
                    FCClientUtil.rotor(origin, center, Direction.Axis.X, (Math.PI / 2).toFloat())

                else -> Unit
            }

            EAEHighlightHandler.highlight(pos, face, dim, endTime, origin)
        }
    }
}
