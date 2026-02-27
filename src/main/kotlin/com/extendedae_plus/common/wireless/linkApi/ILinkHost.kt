package com.extendedae_plus.common.wireless.linkApi

import appeng.api.networking.IGridNode
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import java.util.*

/**
 * 无线端点最小接口。
 * 你的无线收发器方块实体需实现该接口，
 * 以便无线逻辑能够获取世界、位置与 AE2 节点。
 */
interface ILinkHost {
    /** 返回方块所在的服务端世界（避免与 BlockEntity#getLevel 冲突）  */
    val serverLevel: ServerLevel?

    /** 返回方块位置  */
    val blockPos: BlockPos

    /** 返回可用于 AE2 连接的节点（通常为主节点）  */
    val gridNode: IGridNode?

    /** 是否已移除/销毁（端点视角），用于在卸载或破坏时停止连接  */
    val isRemoved: Boolean

    var label: Label

    var placer: UUID?

    var placerName: String

    fun onConnectionChanged(connected: Boolean) {
    }
}
