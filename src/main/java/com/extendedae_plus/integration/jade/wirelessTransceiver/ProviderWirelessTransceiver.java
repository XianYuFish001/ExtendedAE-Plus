package com.extendedae_plus.integration.jade.wirelessTransceiver;

import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver;
import com.extendedae_plus.integration.jade.CommonProviders;
import com.extendedae_plus.integration.jade.ObjectedProvider;
import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.BlockAccessor;

import java.util.function.BiConsumer;

public enum ProviderWirelessTransceiver implements ObjectedProvider.IObjectedProvider<BlockAccessor> {
    MODE((data, accessor) -> {
        var blockState = accessor.getBlockState();
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver)) return;
        data.putBoolean("master_mode", blockState.getValue(BlockWirelessTransceiver.MASTER_MODE));
    }),
    LABEL(CommonProviders.linkLabel),
    CHANNELS(CommonProviders.linkChannels),
    MASTER_LOCATION(CommonProviders.locationMaster(accessor -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver blockEntity))
            return null;
        if (accessor.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE)) return null;
        return blockEntity.getLabel();
    })),
    LOCKED(CommonProviders.stateLocked(BlockWirelessTransceiver.LOCKED)),
    PLACER(CommonProviders.infoPlacer(accessor -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver blockEntity))
            return null;
        return blockEntity.getPlacer();
    }));

    private final BiConsumer<CompoundTag, BlockAccessor> provider;

    ProviderWirelessTransceiver(BiConsumer<CompoundTag, BlockAccessor> provider) {
        this.provider = provider;
    }

    @Override
    public BiConsumer<CompoundTag, BlockAccessor> getProvider() {
        return this.provider;
    }
}
