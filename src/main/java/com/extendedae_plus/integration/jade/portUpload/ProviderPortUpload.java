package com.extendedae_plus.integration.jade.portUpload;

import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockEntityUpload;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload;
import com.extendedae_plus.integration.jade.CommonProviders;
import com.extendedae_plus.integration.jade.ObjectedProvider;
import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.BlockAccessor;

import java.util.function.BiConsumer;

public enum ProviderPortUpload implements ObjectedProvider.IObjectedProvider<BlockAccessor> {
    LABEL(CommonProviders.linkLabel),
    CHANNELS(CommonProviders.linkChannels),
    MASTER_LOCATION(CommonProviders.locationMaster(accessor -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return null;
        return blockEntity.getLabel();
    })),
    LOCKED(CommonProviders.stateLocked(BlockUpload.LOCKED)),
    PLACER(CommonProviders.infoPlacer(accessor -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return null;
        return blockEntity.getPlacer();
    }));

    private final BiConsumer<CompoundTag, BlockAccessor> provider;

    ProviderPortUpload(BiConsumer<CompoundTag, BlockAccessor> provider) {
        this.provider = provider;
    }

    @Override
    public BiConsumer<CompoundTag, BlockAccessor> getProvider() {
        return this.provider;
    }
}
