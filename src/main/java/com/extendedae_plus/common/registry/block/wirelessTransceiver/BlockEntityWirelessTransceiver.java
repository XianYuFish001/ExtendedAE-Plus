package com.extendedae_plus.common.registry.block.wirelessTransceiver;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.util.SettingsFrom;
import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.registry.menu.host.linkLabel.HostLabelLink;
import com.extendedae_plus.common.wireless.LinkMaster;
import com.extendedae_plus.common.wireless.LinkSlave;
import com.extendedae_plus.common.wireless.linkApi.IBlockEntityLabel;
import com.extendedae_plus.common.wireless.linkApi.ILinkHost;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class BlockEntityWirelessTransceiver extends AENetworkedBlockEntity
        implements IBlockEntityLabel, HostLabelLink {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        builder.set(ModDataComponents.SettingsTransceiver, new DataSettings(
                this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE),
                this.host.getLabel(),
                this.host.getPlacer(),
                this.host.getPlacerName()
        ));
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (!(this.getLevel() instanceof ServerLevel level)) return;
        if (!input.has(ModDataComponents.SettingsTransceiver.get())) return;
        var settings = input.get(ModDataComponents.SettingsTransceiver.get());

        if (settings.masterMode != this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE))
            level.setBlock(this.getBlockPos(),
                    this.getBlockState().setValue(BlockWirelessTransceiver.MASTER_MODE, settings.masterMode),
                    Block.UPDATE_CLIENTS);

        this.host.setPlacer(settings.placer());
        this.host.setPlacerName(settings.placerName);
        this.setLabel(settings.label, true);
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
        DataSettings.CODEC.parse(NbtOps.INSTANCE, data.get("data_settings"))
                .resultOrPartial(LOGGER::error)
                .ifPresent(settings -> {
                    this.setLabel(settings.label, true);
                    this.host.setPlacer(settings.placer);
                    this.host.setPlacerName(settings.placerName);
                });
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        DataSettings.CODEC.encodeStart(NbtOps.INSTANCE, new DataSettings(
                false,
                this.getLabel(),
                this.getPlacer(),
                this.getPlacerName()
        )).resultOrPartial(LOGGER::error)
                .ifPresent(dataSettings -> data.put("data_settings", dataSettings));
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

    @Override
    public void setLabel(Label label, boolean force) {
        if (!force && this.getBlockState().getValue(BlockWirelessTransceiver.LOCKED)) return;
        if (this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE))
            this.linkMaster.setLabel(label);
        else this.linkSlave.setLabel(label);
        this.host.setPlacer(label.data.placer());
        this.host.setPlacerName(label.data.placerName());
    }

    public Label getLabel() {
        return this.host.getLabel();
    }

    @Override
    public Label.Data getLabelData() {
        return this.getLabel().data;
    }

    @Override
    public boolean setLabelData(Label.Data label, boolean force) {
        this.setLabel(label.pack(), force);
        return this.getLabelData().equals(label);
    }

    @Override
    public boolean isLockable() {
        return true;
    }

    @Override
    public boolean isMasterable() {
        return true;
    }

    @Override
    public boolean isLocked() {
        return this.getBlockState().getValue(BlockWirelessTransceiver.LOCKED);
    }

    @Override
    public boolean isMaster() {
        return this.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE);
    }

    @Override
    public void toggleLock() {
        var next = !this.isLocked();
        var stateNext = this.getBlockState().setValue(BlockWirelessTransceiver.LOCKED, next);
        if (this.level == null) return;
        this.level.setBlock(this.getBlockPos(), stateNext, Block.UPDATE_CLIENTS);
    }

    @Override
    public void toggleMaster() {
        var next = !this.isMaster();
        var stateNext = this.getBlockState().setValue(BlockWirelessTransceiver.MASTER_MODE, next);
        if (this.level == null) return;
        this.level.setBlock(this.getBlockPos(), stateNext, Block.UPDATE_CLIENTS);
        this.onSwitchMode();
    }

    public @Nullable UUID getPlacer() {
        return this.host.getPlacer();
    }

    public String getPlacerName() {
        return this.host.getPlacerName();
    }

    private class LinkHostTransceiver implements ILinkHost {
        private Label label = Label.EMPTY;
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
        public boolean isRemoved() {
            return BlockEntityWirelessTransceiver.this.isRemoved();
        }

        @Override
        public void onConnectionChanged(boolean connected) {
            if (!(BlockEntityWirelessTransceiver.this.getLevel() instanceof ServerLevel level)) return;
            if (!BlockEntityWirelessTransceiver.this.isRemoved() &&
                    BlockEntityWirelessTransceiver.this.getBlockState()
                            .getValue(BlockWirelessTransceiver.POWERED) != connected) {
                level.setBlock(BlockEntityWirelessTransceiver.this.getBlockPos(),
                        getBlockState().setValue(BlockWirelessTransceiver.POWERED, connected),
                        Block.UPDATE_CLIENTS);
            }
        }

        @Override
        public Label getLabel() {
            return this.label;
        }

        @Override
        public void setLabel(Label label) {
            this.label = label;
        }

        @Override
        public @Nullable UUID getPlacer() {
            return this.placer;
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

    public record DataSettings(boolean masterMode, Label label, @Nullable UUID placer, String placerName) {
        public static final Codec<DataSettings> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.fieldOf("master_mode").forGetter(DataSettings::masterMode),
                Label.Data.CODEC.fieldOf("data_label").forGetter(data -> data.label.data),
                UUIDUtil.CODEC.lenientOptionalFieldOf("placer")
                        .forGetter(data -> Optional.ofNullable(data.placer())),
                Codec.STRING.fieldOf("placer_name").forGetter(DataSettings::placerName)
        ).apply(inst, (masterMode, data, placer, placerName) ->
                new DataSettings(masterMode, data.pack(), placer.orElse(null), placerName)));

        public static final StreamCodec<RegistryFriendlyByteBuf, DataSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, DataSettings::masterMode,
                Label.Data.STREAM_CODEC, data -> data.label.data,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.placer()),
                ByteBufCodecs.STRING_UTF8, DataSettings::placerName,
                (masterMode, data, placer, placerName) ->
                        new DataSettings(masterMode, data.pack(), placer.orElse(null), placerName)
        );
    }
}
