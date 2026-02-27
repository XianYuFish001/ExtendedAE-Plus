package com.extendedae_plus.integration.impl.jade.implementation

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.TileUpload
import com.extendedae_plus.integration.impl.jade.CommonProviders
import com.extendedae_plus.integration.impl.jade.CommonTooltips
import com.extendedae_plus.integration.impl.jade.helper.IObjectedProvider
import com.extendedae_plus.integration.impl.jade.helper.TooltipAppender
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

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
        private val appender: TooltipAppender
    ) : IBlockComponentProvider {
        Channels("channels", CommonTooltips.linkChannels),
        Label("label", CommonTooltips.linkLabel),
        MasterLocation("master_location", CommonTooltips.locationMaster),
        Locked("locked", CommonTooltips.stateLocked),
        Placer("placer", CommonTooltips.infoPlacer);

        private val uid: ResourceLocation = ExtendedAEPlus.getLocation("wireless_transceiver.$path")

        override fun getUid() = this.uid

        override fun appendTooltip(iTooltip: ITooltip, blockAccessor: BlockAccessor, iPluginConfig: IPluginConfig) =
            this.appender.add(this.name, blockAccessor, iTooltip, iPluginConfig)
    }
}
