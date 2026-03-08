package com.extendedae_plus.network

import appeng.api.stacks.AEKey
import com.extendedae_plus.mixin.helper.HelperProviderHost
import com.extendedae_plus.util.UtilKeyBuilder
import com.extendedae_plus.util.UtilNetwork
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import com.fish.fishlib.util.extension.findInstance
import com.fish.fishlib.util.keyBuilder.Patterns
import com.glodblock.github.extendedae.client.render.EAEHighlightHandler
import com.glodblock.github.extendedae.util.FCClientUtil
import com.glodblock.github.glodium.util.GlodUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

/**
 * 客户端从 CraftingCPUScreen 发送：鼠标下条目对应的 AEKey。
 * 服务端在当前打开的 CraftingCPUMenu 所属网络中，定位匹配该 AEKey 的样板供应器，
 * 打开该供应器自身的 UI（不是目标机器的 UI）。
 */

//                        // 部件与方块实体分别选择定位器并打开界面
//                        if (host is AEBasePart) {
//                            host.openMenu(player, MenuLocators.forPart(host))
//                            highlightWithMessage(
//                                pbe.blockPos,
//                                host.side,
//                                Objects.requireNonNull<Level>(pbe.getLevel()).dimension(),
//                                1.0,
//                                player
//                            )
//                        } else {
//                            host.openMenu(player, MenuLocators.forBlockEntity(pbe))
//                            highlightWithMessage(
//                                pbe.blockPos,
//                                null,
//                                Objects.requireNonNull<Level>(pbe.getLevel()).dimension(),
//                                1.0,
//                                player
//                            )
//                        }
//                        // 先在该 provider 中定位 pattern 的槽位索引，以便计算页码（尽量早退出，按槽位逐个解码）
////                        val foundSlot = 0
////                        if (foundSlot >= 0) {
////                            val pageId = foundSlot / 36
////                            if (pageId > 0) {
////                                // 发送 S2C：切换到指定页
////                                player.connection.send(SPacketSetProviderPage(pageId))
////                            }
////                        }
////
////                        // 最后发送高亮包，保证界面已打开
////                        val outs = pattern.getOutputs()
////                        if (outs != null && !outs.isEmpty() && outs.getFirst() != null) {
////                            val key: AEKey = outs.getFirst().what()
////                            player.connection.send(SPacketHighlightPatternSlot(key, true))
@FishNetworkPacket("open_screen_crafting_node_provider")
@JvmRecord
data class CPacketOpenScreenCraftingNodeProvider(val what: AEKey) : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) {
        UtilNetwork.findProviders(player, this.what)
            .findInstance<HelperProviderHost>()
            ?.opener
            ?.invoke(player, null)

        // TODO Highlight
    }

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, CPacketOpenScreenCraftingNodeProvider> =
            StreamCodec.composite(
                AEKey.STREAM_CODEC, CPacketOpenScreenCraftingNodeProvider::what,
                ::CPacketOpenScreenCraftingNodeProvider
            )

        private fun highlightWithMessage(
            pos: BlockPos?,
            face: Direction?,
            dim: ResourceKey<Level>?,
            multiplier: Double,
            player: Player
        ) {
            if (pos == null || dim == null) return
            val endTime = System.currentTimeMillis() + (6000 * GlodUtil.clamp(multiplier, 1.0, 30.0)).toLong()
            if (face == null) {
                EAEHighlightHandler.highlight(pos, dim, endTime)
            } else {
                var origin = AABB(2 / 16.0, 2 / 16.0, 0.0, 14 / 16.0, 14 / 16.0, 2 / 16.0).move(pos)
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

                    else -> {}
                }
                EAEHighlightHandler.highlight(pos, face, dim, endTime, origin)
            }

            player.displayClientMessage(
                UtilKeyBuilder.of(Patterns.Message)
                    .addStr("opened_provider_info")
                    .args(pos.toShortString(), dim.location().path)
                    .build(), false
            )
        }
    }
}
