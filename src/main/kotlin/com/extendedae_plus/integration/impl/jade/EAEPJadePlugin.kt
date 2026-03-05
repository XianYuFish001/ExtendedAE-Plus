package com.extendedae_plus.integration.impl.jade

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.ExtendedAEPlus.Companion.location
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.TileUpload
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver
import com.extendedae_plus.common.registry.block.wirelessTransceiver.TileWirelessTransceiver
import com.extendedae_plus.integration.impl.jade.impl.PortUpload
import com.extendedae_plus.integration.impl.jade.impl.WirelessTransceiver
import com.fish.fishlib.integration.jade.InfoBlock
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin

@WailaPlugin(ExtendedAEPlus.MODID)
class EAEPJadePlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        blocks.forEach { it.registerProvider(registration) }
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        blocks.forEach { it.registerConsumer(registration) }
    }

    companion object {
        private val blocks = listOf(
            InfoBlock(
                "provider_wireless_transceiver".location(),
                WirelessTransceiver.Provider::class,
                WirelessTransceiver.Tooltip::class,
                TileWirelessTransceiver::class,
                BlockWirelessTransceiver::class
            ),
            InfoBlock(
                "provider_port_upload".location(),
                PortUpload.Provider::class,
                PortUpload.Tooltip::class,
                TileUpload::class,
                BlockUpload::class
            )
        )
    }
}