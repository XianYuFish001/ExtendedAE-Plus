package com.extendedae_plus.integration.impl.jade

import appeng.me.helpers.IGridConnectedBlockEntity
import com.extendedae_plus.common.registry.block.wirelessTransceiver.TileWirelessTransceiver
import com.extendedae_plus.common.wireless.linkApi.IBlockEntityLabel
import com.extendedae_plus.common.wireless.linkApi.Label
import com.extendedae_plus.common.wireless.linkApi.RegistryLink
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationFTBTeams
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.properties.Property
import snownee.jade.api.BlockAccessor
import java.util.*
import kotlin.jvm.optionals.getOrNull

private typealias Function<A, B> = (A) -> B

object CommonProviders {
    @JvmField
    val linkLabel = provider@{ data: CompoundTag, accessor: BlockAccessor ->
        val tile = accessor.blockEntity as? IBlockEntityLabel ?: return@provider
        val label = tile.label.data
        if (label.isEmpty) {
            data.putBoolean("unset", true)
        } else if (label.frequency != null) {
            data.putLong("frequency", label.frequency)
        } else data.putString("label", label.label)
    }

    @JvmField
    val linkChannels = provider@{ data: CompoundTag, accessor: BlockAccessor ->
        val node = (accessor.blockEntity as? IGridConnectedBlockEntity)?.gridNode ?: return@provider

        var usedChannels = 0
        for (connection in node.connections) {
            usedChannels = connection.usedChannels.coerceAtLeast(usedChannels)
        }
        data.putInt("used", usedChannels)
        data.putInt("max", node.maxChannels)
    }

    @JvmStatic
    fun locationMaster(infoExternal: Function<BlockAccessor, Label?>) =
        provider@{ data: CompoundTag, accessor: BlockAccessor ->
            val label = infoExternal(accessor) ?: return@provider

            val masterHost = RegistryLink.findMaster(label)
            if (masterHost == null || masterHost.isRemoved) return@provider

            val pos = masterHost.blockPos
            val level = masterHost.serverLevel
            data.putLong("pos", masterHost.blockPos.asLong())
            if (level != null) data.putString("dim", level.dimension().location().toString())
            if (level != null) {
                val blockEntity = level.getBlockEntity(pos)
                        as? TileWirelessTransceiver
                    ?: return@provider
                if (blockEntity.hasCustomName())
                    data.putString("name", blockEntity.customName?.string ?: return@provider)
            }
        }

    @JvmStatic
    fun stateLocked(propertyLocked: Property<Boolean>) = provider@{ data: CompoundTag, accessor: BlockAccessor ->
        val blockState = accessor.blockState
        val locked = blockState
            .getOptionalValue(propertyLocked)
            .getOrNull()
            ?: return@provider
        data.putBoolean("locked", locked)
    }

    @JvmStatic
    fun infoPlacer(infoExternal: Function<BlockAccessor, UUID?>) =
        provider@{ data: CompoundTag, accessor: BlockAccessor ->
            val placer = infoExternal(accessor) ?: return@provider
            data.putUUID("uuid", placer)
            data.putString("name", ManagerIntegration<IntegrationFTBTeams>()
                ?.getTeamName(placer)
                ?.string ?: return@provider)
        }
}
