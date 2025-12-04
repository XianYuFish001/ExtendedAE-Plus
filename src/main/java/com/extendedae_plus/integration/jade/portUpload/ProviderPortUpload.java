package com.extendedae_plus.integration.jade.portUpload;

import appeng.api.networking.IGridConnection;
import com.extendedae_plus.common.block.assemblerMatrix.portUpload.BlockEntityUpload;
import com.extendedae_plus.common.block.assemblerMatrix.portUpload.BlockUpload;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.wireless.linkApi.LinkRegistry;
import com.extendedae_plus.integration.jade.CommonProvider;
import com.extendedae_plus.util.WirelessTeamUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.BlockAccessor;

import java.util.function.BiConsumer;

public enum ProviderPortUpload implements CommonProvider.IObjectedProvider {
    FREQUENCY((data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return;
        data.putLong("frequency", blockEntity.getFrequency());
    }),
    CHANNELS((data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return;
        var node = blockEntity.getGridNode();
        if (node == null) return;

        int usedChannels = 0;
        for (IGridConnection connection : node.getConnections()) {
            usedChannels = Math.max(connection.getUsedChannels(), usedChannels);
        }
        data.putInt("usedChannels", usedChannels);
        data.putInt("maxChannels", node.getMaxChannels());
    }),
    MASTER_LOCATION((data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return;

        var info = blockEntity.getLinkInfo();
        if (info == null) return;

        var masterHost = LinkRegistry.findMaster(info);
        if (masterHost == null || masterHost.isEndpointRemoved()) return;

        var pos = masterHost.getBlockPos();
        var level = masterHost.getServerLevel();
        if (pos != null)
            data.putLong("masterPos", masterHost.getBlockPos().asLong());
        if (level != null)
            data.putString("masterDim", level.dimension().location().toString());
        if (pos != null && level != null) {
            if (level.getBlockEntity(pos) instanceof BlockEntityWirelessTransceiver masterBlockEntity
                    && masterBlockEntity.hasCustomName())
                data.putString("masterName", masterBlockEntity.getCustomName().getString());
        }
    }),
    LOCKED((data, accessor) -> {
        var blockState = accessor.getBlockState();
        if (!(accessor.getBlockEntity() instanceof BlockEntityUpload)) return;

        data.putBoolean("locked", blockState.getValue(BlockUpload.LOCKED));
    }),
    PLACER((data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return;

        var placer = blockEntity.getPlacer();
        if (placer == null) return;
        data.putUUID("placer", placer);

        if (!(blockEntity.getLevel() instanceof ServerLevel level)) return;
        data.putString("placerName", WirelessTeamUtil.getNetworkOwnerName(level, placer).getString());
    });

    private final BiConsumer<CompoundTag, BlockAccessor> provider;

    ProviderPortUpload(BiConsumer<CompoundTag, BlockAccessor> provider) {
        this.provider = provider;
    }

    @Override
    public BiConsumer<CompoundTag, BlockAccessor> getProvider() {
        return this.provider;
    }
}
