package com.extendedae_plus.integration.jade;

import appeng.api.networking.IGridConnection;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.wireless.linkApi.IBlockEntityLabel;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.extendedae_plus.common.wireless.linkApi.RegistryLink;
import com.extendedae_plus.integration.IntegrationFTBTeams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class CommonProviders {
    public static final BiConsumer<CompoundTag, BlockAccessor> linkLabel = (data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof IBlockEntityLabel blockEntity)) return;

        var label = blockEntity.getLabel().data;
        if (label.isEmpty()) {
            data.putBoolean("unset", true);
        } else if (label.frequency() != null) {
            data.putLong("frequency", label.frequency());
        } else data.putString("label", label.label());
    };

    public static final BiConsumer<CompoundTag, BlockAccessor> linkChannels = (data, accessor) -> {
        if (!(accessor.getBlockEntity() instanceof IGridConnectedBlockEntity blockEntity)) return;
        var node = blockEntity.getGridNode();
        if (node == null) return;

        int usedChannels = 0;
        for (IGridConnection connection : node.getConnections()) {
            usedChannels = Math.max(connection.getUsedChannels(), usedChannels);
        }
        data.putInt("used", usedChannels);
        data.putInt("max", node.getMaxChannels());
    };

    public static BiConsumer<CompoundTag, BlockAccessor> locationMaster(Function<BlockAccessor, Label> infoExternal) {
        return (data, accessor) -> {
            var label = infoExternal.apply(accessor);
            if (label == null) return;

            var masterHost = RegistryLink.findMaster(label);
            if (masterHost == null || masterHost.isRemoved()) return;

            var pos = masterHost.getBlockPos();
            var level = masterHost.getServerLevel();
            if (pos != null)
                data.putLong("pos", masterHost.getBlockPos().asLong());
            if (level != null)
                data.putString("dim", level.dimension().location().toString());
            if (pos != null && level != null) {
                if (level.getBlockEntity(pos) instanceof BlockEntityWirelessTransceiver masterBlockEntity
                        && masterBlockEntity.hasCustomName())
                    data.putString("name", masterBlockEntity.getCustomName().getString());
            }
        };
    }

    public static BiConsumer<CompoundTag, BlockAccessor> stateLocked(BooleanProperty propertyLocked) {
        return (data, accessor) -> {
            var blockState = accessor.getBlockState();
            var locked = blockState.getOptionalValue(propertyLocked);
            if (locked.isEmpty()) return;
            data.putBoolean("locked", locked.get());
        };
    }

    public static BiConsumer<CompoundTag, BlockAccessor> infoPlacer(Function<BlockAccessor, @Nullable UUID> infoExternal) {
        return (data, accessor) -> {
            var placer = infoExternal.apply(accessor);
            if (placer == null) return;
            data.putUUID("uuid", placer);
            data.putString("name", IntegrationFTBTeams.instance.getTeamName(placer).getString());
        };
    }
}
