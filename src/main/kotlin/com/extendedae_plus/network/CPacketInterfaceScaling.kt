package com.extendedae_plus.network

import appeng.api.stacks.GenericStack
import appeng.menu.implementations.InterfaceMenu
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToServer
import com.fish.fishlib.util.UtilMath
import com.glodblock.github.extendedae.container.ContainerExInterface
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

/**
 * C2S：调整 ME 接口配置槽位(标记物品)的数量。
 * 支持按因子倍增或整除，且保持最小值为 1。
 */
@FishNetworkPacket("interface_scaling")
@JvmRecord
data class CPacketInterfaceScaling(val scale: Int) : CPacketGeneric {
    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<FriendlyByteBuf, CPacketInterfaceScaling> =
            StreamCodec.composite(
                ByteBufCodecs.INT, CPacketInterfaceScaling::scale,
                ::CPacketInterfaceScaling
            )

        @JvmStatic
        fun send(action: EAEPActionItems) {
            val scale = when (action) {
                EAEPActionItems.Mul2 -> 2
                EAEPActionItems.Mul3 -> 3
                EAEPActionItems.Mul5 -> 5
                EAEPActionItems.Div2 -> -2
                EAEPActionItems.Div3 -> -3
                EAEPActionItems.Div5 -> -5
                else -> return
            }
            CPacketInterfaceScaling(scale).sendToServer()
        }
    }

    override fun handleServer(player: ServerPlayer) {
        val logic = when (val menu = player.containerMenu) {
            is InterfaceMenu -> menu.host
            is ContainerExInterface -> menu.host
            else -> return
        }.interfaceLogic

        val inv = logic.config
        for (indexStack in 0..<inv.size()) {
            var stack = inv.getStack(indexStack) ?: continue
            val amount = UtilMath.scale(stack.amount, this.scale.toLong(), true)

            stack = GenericStack(stack.what, amount)
            inv.setStack(indexStack, stack)
        }
    }
}
