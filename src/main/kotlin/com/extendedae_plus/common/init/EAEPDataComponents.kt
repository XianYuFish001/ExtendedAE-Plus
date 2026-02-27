package com.extendedae_plus.common.init

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.TileUpload
import com.extendedae_plus.common.registry.block.wirelessTransceiver.TileWirelessTransceiver
import com.extendedae_plus.common.registry.dataComponent.DataChannelCard
import com.extendedae_plus.common.registry.dataComponent.DataEncoderProfile
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority
import com.fish.fishlib.common.InitObject
import com.mojang.serialization.Codec
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

// TODO Interfaced CODEC
object EAEPDataComponents {
    @InitObject
    val Register: DeferredRegister.DataComponents =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ExtendedAEPlus.MODID)

    @JvmField
    val CardChannel = this.register("data_channel_card", DataChannelCard.codec, DataChannelCard.streamCodec)
    @JvmField
    val CardTicking = this.register("data_ticking_card", DataTickingCard.codec, DataTickingCard.streamCodec)
    @JvmField
    val SettingsTransceiver = this.register(
        "data_transceiver_settings",
        TileWirelessTransceiver.DataSettings.codec,
        TileWirelessTransceiver.DataSettings.streamCodec
    )
    @JvmField
    val SettingsPortUpload = this.register(
        "data_port_upload_settings",
        TileUpload.DataSettings.codec,
        TileUpload.DataSettings.streamCodec
    )
    @JvmField
    val Priority = this.register("data_priority", DataPriority.codec, DataPriority.streamCodec)
    @JvmField
    val ProfileEncoder = this.register("data_encoder_profile", DataEncoderProfile.codec, DataEncoderProfile.streamCodec)

    private fun <T> register(
        name: String, codec: Codec<T>, streamCodec: StreamCodec<RegistryFriendlyByteBuf, T>
    ): DeferredHolder<DataComponentType<*>, DataComponentType<T>> = Register.registerComponentType<T>(
        name
    ) { it.persistent(codec).networkSynchronized(streamCodec) }
}
