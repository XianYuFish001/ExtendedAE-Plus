package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.dataComponent.DataChannelCard;
import com.extendedae_plus.common.dataComponent.DataEncoderProfile;
import com.extendedae_plus.common.dataComponent.DataSpeedCard;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENT =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ExtendedAEPlus.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataChannelCard>> DATA_CHANNEL_CARD =
            register("data_channel_card", DataChannelCard.CODEC, DataChannelCard.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataSpeedCard>> DATA_SPEED_CARD =
            register("data_speed_card", DataSpeedCard.CODEC, DataSpeedCard.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>,
            DataComponentType<BlockEntityWirelessTransceiver.DataSettings>> DATA_SETTINGS =
            register("data_transceiver_settings",
                    BlockEntityWirelessTransceiver.DataSettings.CODEC,
                    BlockEntityWirelessTransceiver.DataSettings.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataEncoderProfile>> DATA_ENCODER_PROFILE =
            register("data_encoder_profile", DataEncoderProfile.CODEC, DataEncoderProfile.STREAM_CODEC);

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return COMPONENT.registerComponentType(name,
                builder -> builder.persistent(codec).networkSynchronized(streamCodec));
    }
}
