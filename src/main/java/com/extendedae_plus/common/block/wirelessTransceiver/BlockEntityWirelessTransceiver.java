package com.extendedae_plus.common.block.wirelessTransceiver;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.util.SettingsFrom;
import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.wireless.LinkMaster;
import com.extendedae_plus.common.wireless.LinkSlave;
import com.extendedae_plus.common.wireless.linkApi.ILinkHost;
import com.extendedae_plus.common.wireless.linkApi.LinkRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BlockEntityWirelessTransceiver extends AENetworkedBlockEntity {
    private final LinkHostTransceiver host;

    private final LinkMaster linkMaster;
    private final LinkSlave linkSlave;

    public BlockEntityWirelessTransceiver(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WIRELESS_TRANSCEIVER.get(), pos, blockState);
        this.host = new LinkHostTransceiver();
        this.linkMaster = new LinkMaster(this.host);
        this.linkSlave = new LinkSlave(this.host);

        this.getMainNode()
                .setFlags(GridFlags.DENSE_CAPACITY)
                .setTagName("wireless_transceiver_node")
                .setVisualRepresentation(this.getItemFromBlockEntity());
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.linkMaster.onUnloadOrRemove();
        this.linkSlave.onUnloadOrRemove();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.linkMaster.onUnloadOrRemove();
        this.linkSlave.onUnloadOrRemove();
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, new BlockEntityNodeListener<>() {
            @Override
            public void onStateChanged(BlockEntityWirelessTransceiver nodeOwner, IGridNode node, State state) {
                super.onStateChanged(nodeOwner, node, state);
                if (!(getLevel() instanceof ServerLevel level)) return;
                if (!state.equals(State.CHANNEL)) return;

                boolean connected;
                if (nodeOwner.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE))
                    connected = true;
                else connected = linkSlave.connected();

                if (!nodeOwner.isRemoved() &&
                        nodeOwner.getBlockState().getValue(BlockWirelessTransceiver.POWERED) != connected) {
                    level.setBlock(worldPosition,
                            getBlockState().setValue(BlockWirelessTransceiver.POWERED, connected),
                            Block.UPDATE_CLIENTS);
                }
            }
        });
    }

    public void onSwitchMode() {
        this.linkMaster.unregister();
        this.linkSlave.unregister();

        if (this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE))
            this.linkMaster.register();
        else this.linkSlave.register();
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player) {
        super.exportSettings(mode, builder, player);

        if (mode != SettingsFrom.MEMORY_CARD) return;
        builder.set(ModDataComponents.DATA_SETTINGS, new DataSettings(
                this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE),
                this.host.getFrequency(),
                this.host.getPlacer(),
                this.host.getPlacerName()
        ));
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (!(this.getLevel() instanceof ServerLevel level)) return;
        if (!input.has(ModDataComponents.DATA_SETTINGS.get())) return;
        var settings = input.get(ModDataComponents.DATA_SETTINGS.get());

        if (settings.masterMode != this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE))
            level.setBlock(this.getBlockPos(),
                    this.getBlockState().setValue(BlockWirelessTransceiver.MASTER_MODE, settings.masterMode),
                    Block.UPDATE_CLIENTS);

        this.host.setPlacer(settings.placer());
        this.host.setPlacerName(settings.placerName);
        this.setFrequency(settings.frequency, false);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        GridHelper.onFirstTick(this, blockEntity -> {
            if (this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE))
                this.linkMaster.register();
            else this.linkSlave.register();
        });
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        if (data.hasUUID("placer"))
            this.host.setPlacer(data.getUUID("placer"));
        if (data.contains("placer_name"))
            this.host.setPlacerName(data.getString("placer_name"));
        if (data.contains("frequency"))
            this.setFrequency(data.getLong("frequency"), true);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putLong("frequency", this.getFrequency());
        if (this.host.getPlacer() != null)
            data.putUUID("placer", this.host.getPlacer());
        if (!this.host.getPlacerName().isEmpty())
            data.putString("placer_name", this.host.getPlacerName());
    }

    @Override
    public void setOwner(Player owner) {
        super.setOwner(owner);
        var id = owner.getUUID();
        var name = owner.getName().getString();
        this.host.setPlacerName(name);
        this.linkMaster.setPlacer(id);
        this.linkSlave.setPlacer(id);
    }

    public void setFrequency(long frequency, boolean force) {
        if (!force && this.getBlockState().getValue(BlockWirelessTransceiver.LOCKED)) return;
        if (this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE))
            this.linkMaster.setFrequency(frequency);
        else this.linkSlave.setFrequency(frequency);
    }

    public LinkRegistry.LinkInfo getLinkInfo() {
        return LinkRegistry.LinkInfo.fromHost(host);
    }

    public long getFrequency() {
        return this.host.getFrequency();
    }

    public @Nullable UUID getPlacer() {
        return this.host.getPlacer();
    }

    public String getPlacerName() {
        return this.host.getPlacerName();
    }

    private class LinkHostTransceiver implements ILinkHost {
        private long frequency = 0;
        @Nullable
        private UUID placer = null;
        private String placerName = "";

        @Override
        public @Nullable ServerLevel getServerLevel() {
            var level = BlockEntityWirelessTransceiver.this.getLevel();
            return level instanceof ServerLevel serverLevel ? serverLevel : null;
        }

        @Override
        public BlockPos getBlockPos() {
            return BlockEntityWirelessTransceiver.this.getBlockPos();
        }

        @Override
        public IGridNode getGridNode() {
            return BlockEntityWirelessTransceiver.this.getGridNode();
        }

        @Override
        public boolean isEndpointRemoved() {
            return BlockEntityWirelessTransceiver.this.isRemoved();
        }

        @Override
        public void updateBlockState() {
        }

        @Override
        public long getFrequency() {
            return this.frequency;
        }

        @Override
        public @Nullable UUID getPlacer() {
            return this.placer;
        }

        @Override
        public void setFrequency(long frequency) {
            this.frequency = frequency;
        }

        @Override
        public void setPlacer(@Nullable UUID placer) {
            this.placer = placer;
        }

        public String getPlacerName() {
            return this.placerName;
        }

        public void setPlacerName(String placerName) {
            this.placerName = placerName;
        }
    }

    public record DataSettings(boolean masterMode, long frequency, @Nullable UUID placer, String placerName) {
        public static final Codec<DataSettings> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.fieldOf("master_mode").forGetter(DataSettings::masterMode),
                Codec.LONG.fieldOf("frequency").forGetter(DataSettings::frequency),
                UUIDUtil.CODEC.lenientOptionalFieldOf("placer").forGetter(data -> Optional.ofNullable(data.placer())),
                Codec.STRING.fieldOf("placer_name").forGetter(DataSettings::placerName)
        ).apply(inst, (masterMode, frequency, placer, placerName) ->
                new DataSettings(masterMode, frequency, placer.orElse(null), placerName)));

        public static final StreamCodec<RegistryFriendlyByteBuf, DataSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, DataSettings::masterMode,
                ByteBufCodecs.VAR_LONG, DataSettings::frequency,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.placer()),
                ByteBufCodecs.STRING_UTF8, DataSettings::placerName,
                (masterMode, frequency, placer, placerName) ->
                        new DataSettings(masterMode, frequency, placer.orElse(null), placerName)
        );
    }
}
