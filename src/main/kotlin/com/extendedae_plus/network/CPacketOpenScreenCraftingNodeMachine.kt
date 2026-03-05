package com.extendedae_plus.network

import appeng.api.networking.IGrid
import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.networking.security.IActionHost
import appeng.api.stacks.AEKey
import appeng.blockentity.AEBaseBlockEntity
import appeng.blockentity.crafting.PatternProviderBlockEntity
import appeng.blockentity.misc.InterfaceBlockEntity
import appeng.blockentity.networking.CableBusBlockEntity
import appeng.helpers.patternprovider.PatternProviderLogic
import appeng.me.service.CraftingService
import appeng.menu.me.crafting.CraftingCPUMenu
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationMekanism
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorProviderLogic
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

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
        val level = player.serverLevel()
        val menu = player.containerMenu
        if (menu !is CraftingCPUMenu) return

        // 通过菜单 target（可能是 BlockEntity/Part/ItemHost）按 IActionHost 获取 Grid
        var grid: IGrid? = null
        val target = menu.getTarget()
        if (target is IActionHost) grid = target.actionableNode?.grid
        if (grid == null) return

        val serviceCrafting = grid.craftingService
        if (serviceCrafting !is CraftingService) return

        // 根据 AEKey 找到可能的 Pattern, 遍历提供该样板的 Provider
        val providers: MutableList<ICraftingProvider> = ArrayList<ICraftingProvider>()
        serviceCrafting.getCraftingFor(this.what)
            .map(serviceCrafting::getProviders)
            .forEach { it.forEach(providers::addLast) }

        providers.forEach { provider: ICraftingProvider ->
            if (provider !is PatternProviderLogic) return@forEach
            // 使用 accessor 获取 host（受保护字段通过 accessor 访问）
            val host = (provider as AccessorProviderLogic).getHost() ?: return@forEach
            val blockEntityProvider = host.blockEntity

            val delayedBlocks = ArrayList<Direction>()

            // 尝试对邻居打开 GUI（优先通过 MenuProvider）
            // TODO Refactor
            for (face in host.targets) {
                val targetPos = blockEntityProvider.blockPos.relative(face)

                val blockEntityTarget = level.getBlockEntity(targetPos)
                if (blockEntityTarget is MenuProvider) {
                    player.openMenu(blockEntityTarget, targetPos)
                    return@forEach
                }

                val blockStateTarget = level.getBlockState(targetPos)
                blockStateTarget.getMenuProvider(level, targetPos)?.let {
                    player.openMenu(it, targetPos)
                    return@forEach
                }

                // awc这AE怎么这么坏啊😭😭😭
                if (blockEntityTarget is AEBaseBlockEntity) {
                    when (blockEntityTarget) {
                        is InterfaceBlockEntity -> delayedBlocks.addFirst(face)
                        is PatternProviderBlockEntity -> delayedBlocks.addLast(face)
                        is CableBusBlockEntity -> delayedBlocks.addFirst(face)
                        else -> blockStateTarget.useWithoutItem(
                            level, player,
                            BlockHitResult(
                                player.position(),
                                face.opposite,
                                targetPos,
                                false
                            )
                        )
                    }
                    continue
                }

                if (blockEntityTarget == null) continue
                ManagerIntegration<IntegrationMekanism>()?.openGui(blockEntityTarget, player)
//                val blockEntityClassName = blockEntityTarget.javaClass.getName().lowercase()
//                if (blockEntityClassName.contains("mekanism") && blockEntityClassName.contains("tile")) {
//                    try {
//                        val methodOpenGui = blockEntityTarget.javaClass.getMethod("openGui", Player::class.java)
//                        methodOpenGui.invoke(blockEntityTarget, player as Player)
//                    } catch (_: NoSuchMethodException) {
//                    } catch (_: IllegalAccessException) {
//                    } catch (_: InvocationTargetException) {
//                    }
//                    return@forEach
//                }
            }

            if (delayedBlocks.isEmpty()) return@forEach
            val pos = blockEntityProvider.blockPos.relative(delayedBlocks[0])

            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is CableBusBlockEntity) {
                blockEntity.cableBus.getPart(delayedBlocks[0].opposite)?.onUseWithoutItem(
                    player,
                    Vec3(
                        pos.x.toDouble(),
                        pos.y.toDouble(),
                        pos.z.toDouble()
                    )
                )
            } else {
                level.getBlockState(pos).useWithoutItem(
                    level, player, BlockHitResult(
                        player.position(),
                        delayedBlocks[0].opposite,
                        pos,
                        false
                    )
                )
            }
        }
    }
}
