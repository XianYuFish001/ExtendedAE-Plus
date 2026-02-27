package com.extendedae_plus.common.wireless.host

import appeng.api.networking.IGridNode
import com.extendedae_plus.common.wireless.linkApi.ILinkHost
import com.extendedae_plus.common.wireless.linkApi.Label
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import java.util.*

class HostGeneric(
    private val getterBlockEntity: () -> BlockEntity?,
    private val getterNode: () -> IGridNode?
) : ILinkHost {
    override var label = Label.Empty
    override var placer: UUID? = null
    override var placerName: String = ""

    override val serverLevel: ServerLevel?
        get() = this.getterBlockEntity()?.level as? ServerLevel

    override val blockPos: BlockPos
        get() = this.getterBlockEntity()?.blockPos ?: BlockPos.ZERO

    override val gridNode: IGridNode?
        get() = this.getterNode()

    override val isRemoved: Boolean
        get() = this.getterBlockEntity()?.isRemoved != false
}
