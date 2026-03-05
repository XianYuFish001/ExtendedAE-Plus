package com.extendedae_plus.integration.impl.jade.impl

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.TileUpload
import com.extendedae_plus.integration.impl.jade.CommonProviders
import com.extendedae_plus.integration.impl.jade.CommonTooltips
import com.fish.fishlib.integration.jade.IObjectedAppenderBlock
import com.fish.fishlib.integration.jade.IObjectedProvider
import com.fish.fishlib.integration.jade.TooltipAppender
import net.minecraft.nbt.CompoundTag
import snownee.jade.api.BlockAccessor

class PortUpload {
    enum class Provider(
        override val provider: (CompoundTag, BlockAccessor) -> Unit
    ) : IObjectedProvider<BlockAccessor> {
        Label(CommonProviders.linkLabel),
        Channels(CommonProviders.linkChannels),
        MasterLocation(CommonProviders.locationMaster { accessor ->
            (accessor.blockEntity as? TileUpload)?.label
        }),
        Locked(CommonProviders.stateLocked(BlockUpload.PropertyLocked)),
        Placer(CommonProviders.infoPlacer { accessor ->
            (accessor.blockEntity as? TileUpload)?.placer
        })
    }

    enum class Tooltip(
        path: String,
        override val appender: TooltipAppender
    ) : IObjectedAppenderBlock {
        Channels("channels", CommonTooltips.linkChannels),
        Label("label", CommonTooltips.linkLabel),
        MasterLocation("master_location", CommonTooltips.locationMaster),
        Locked("locked", CommonTooltips.stateLocked),
        Placer("placer", CommonTooltips.infoPlacer);

        override val id = ExtendedAEPlus.getLocation("wireless_transceiver.$path")
    }
}
