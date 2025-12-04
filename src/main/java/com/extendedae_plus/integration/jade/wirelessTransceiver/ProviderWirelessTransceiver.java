package com.extendedae_plus.integration.jade.wirelessTransceiver;

import appeng.api.networking.IGridConnection;
import com.extendedae_plus.common.api.IBlockEntityFrequency;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockWirelessTransceiver;
import com.extendedae_plus.common.wireless.linkApi.LinkRegistry;
import com.extendedae_plus.integration.jade.CommonProvider;
import com.extendedae_plus.util.WirelessTeamUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.BlockAccessor;

import java.util.function.BiConsumer;

public enum ProviderWirelessTransceiver implements CommonProvider.IObjectedProvider {
    MASTER_MODE((data, accessor) -> {
        var blockState = accessor.getBlockState();
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver)) return;
        data.putBoolean("masterMode", blockState.getValue(BlockWirelessTransceiver.MASTER_MODE));
    }),
    FREQUENCY((data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof IBlockEntityFrequency blockEntity)) return;
        data.putLong("frequency", blockEntity.getFrequency());
    }),
    CHANNELS((data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver blockEntity)) return;
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
        var blockState = accessor.getBlockState();
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver blockEntity)) return;
        if (!blockState.getValue(BlockWirelessTransceiver.MASTER_MODE)) return;

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
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver)) return;

        data.putBoolean("locked", blockState.getValue(BlockWirelessTransceiver.LOCKED));
    }),
    PLACER((data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver blockEntity)) return;

        var placer = blockEntity.getPlacer();
        if (placer == null) return;
        data.putUUID("placer", placer);

        if (!(blockEntity.getLevel() instanceof ServerLevel level)) return;
        data.putString("placerName", WirelessTeamUtil.getNetworkOwnerName(level, placer).getString());
    });

    private final BiConsumer<CompoundTag, BlockAccessor> provider;

    ProviderWirelessTransceiver(BiConsumer<CompoundTag, BlockAccessor> provider) {
        this.provider = provider;
    }

    @Override
    public BiConsumer<CompoundTag, BlockAccessor> getProvider() {
        return this.provider;
    }
}
