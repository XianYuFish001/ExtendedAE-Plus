package com.extendedae_plus.integration.impl.jade

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.TileUpload
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver
import com.extendedae_plus.common.registry.block.wirelessTransceiver.TileWirelessTransceiver
import com.extendedae_plus.integration.impl.jade.helper.IObjectedProvider
import com.extendedae_plus.integration.impl.jade.helper.WrapperObjectedProvider
import com.extendedae_plus.integration.impl.jade.implementation.PortUpload
import com.extendedae_plus.integration.impl.jade.implementation.WirelessTransceiver
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import snownee.jade.api.*
import kotlin.reflect.KClass

@WailaPlugin(ExtendedAEPlus.MODID)
class EAEPJadePlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        // 注册服务端数据提供者（用于同步数据）
        blocks.forEach { it.registerProvider(registration) }
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        // 遍历组件常量，逐一注册
        blocks.forEach { it.registerConsumer(registration) }
    }

    private data class InfoBlock<TProvider, TConsumer>(
        val uid: String,
        val clazzProvider: KClass<TProvider>,
        val clazzConsumer: KClass<TConsumer>,
        val clazzBlockEntity: KClass<out BlockEntity>,
        val clazzBlock: KClass<out Block>
    ) where TProvider : Enum<TProvider>,
            TProvider : IObjectedProvider<BlockAccessor>,
            TConsumer : Enum<TConsumer>,
            TConsumer : IBlockComponentProvider {
        fun registerProvider(registration: IWailaCommonRegistration) {
            registration.registerBlockDataProvider(
                WrapperObjectedProvider.create(this.uid, this.clazzProvider.java),
                this.clazzBlockEntity.java
            )
        }

        fun registerConsumer(registration: IWailaClientRegistration) {
            for (entry in this.clazzConsumer.java.getEnumConstants()) {
                registration.registerBlockComponent(entry, this.clazzBlock.java)
            }
        }
    }

    companion object {
        private val blocks = mutableListOf(
            InfoBlock(
                "provider_wireless_transceiver",
                WirelessTransceiver.Provider::class,
                WirelessTransceiver.Tooltip::class,
                TileWirelessTransceiver::class,
                BlockWirelessTransceiver::class
            ),
            InfoBlock(
                "provider_port_upload",
                PortUpload.Provider::class,
                PortUpload.Tooltip::class,
                TileUpload::class,
                BlockUpload::class
            )
        )
    }
}