package com.extendedae_plus.integration.impl.jade.implementation

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver
import com.extendedae_plus.common.registry.block.wirelessTransceiver.TileWirelessTransceiver
import com.extendedae_plus.integration.impl.jade.CommonProviders
import com.extendedae_plus.integration.impl.jade.CommonTooltips
import com.extendedae_plus.integration.impl.jade.helper.IObjectedProvider
import com.extendedae_plus.integration.impl.jade.helper.TooltipAppender
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class WirelessTransceiver {
    enum class Provider(
        override val provider: (CompoundTag, BlockAccessor) -> Unit
    ) : IObjectedProvider<BlockAccessor> {
        Mode(provider@{ data, accessor ->
            val blockState = accessor.blockState
            if (accessor.blockEntity !is TileWirelessTransceiver) return@provider
            data.putBoolean("master_mode", blockState.getValue(BlockWirelessTransceiver.PropertyMaster))
        }),
        Label(CommonProviders.linkLabel),
        Channels(CommonProviders.linkChannels),
        MasterLocation(CommonProviders.locationMaster { accessor ->
            val tile = accessor.blockEntity as? TileWirelessTransceiver ?: return@locationMaster null
            if (accessor.blockEntity !is TileWirelessTransceiver) return@locationMaster null
            if (accessor.blockState.getValue(BlockWirelessTransceiver.PropertyMaster))
                return@locationMaster null
            tile.label
        }),
        Locked(CommonProviders.stateLocked(BlockWirelessTransceiver.PropertyLocked)),
        Placer(CommonProviders.infoPlacer { accessor ->
            (accessor.blockEntity as? TileWirelessTransceiver)?.placer
        })
    }

    enum class Tooltip(path: String, private val adder: TooltipAppender) : IBlockComponentProvider {
        Channels("channels", CommonTooltips.linkChannels),
        Label("label", CommonTooltips.linkLabel),
        Mode(
            "master_mode",
            TooltipAppender { _, tooltip, _, data ->
                if (!data.contains("master_mode")) return@TooltipAppender
                val masterMode = data.getBoolean("master_mode")
                tooltip.add(
                    UtilKeyBuilder.of(Patterns.JadeInfo)
                        .item(EAEPItems.WirelessTransceiver)
                        .addStr("mode")
                        .addStr(masterMode, "master", "slave")
                        .build()
                )
            }),
        MasterLocation("master_location", CommonTooltips.locationMaster),
        Locked("locked", CommonTooltips.stateLocked),
        Placer("placer", CommonTooltips.infoPlacer);

        private val uid: ResourceLocation = ExtendedAEPlus.getLocation("wireless_transceiver.$path")

        override fun getUid() = this.uid

        override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
            this.adder.add(this.name, accessor, tooltip, config)
        }
    }
}
