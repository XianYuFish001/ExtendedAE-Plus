package com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.util.SettingsFrom;
import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.registry.menu.host.linkLabel.HostLabelLink;
import com.extendedae_plus.common.wireless.LinkSlave;
import com.extendedae_plus.common.wireless.host.HostGeneric;
import com.extendedae_plus.common.wireless.linkApi.IBlockEntityLabel;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.extendedae_plus.mixin.impl.bridge.HelperAssemblerMatrixModifier;
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixWall;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class BlockEntityUpload extends TileAssemblerMatrixWall
        implements IBlockEntityLabel, HostLabelLink {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final HostGeneric host;
    private final LinkSlave linkSlave;
    
    public BlockEntityUpload(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PORT_UPLOAD.get(), pos, blockState);
        
        this.host = new HostGeneric(this::getBlockEntity, this.getMainNode()::getNode);
        this.linkSlave = new LinkSlave(this.host);

        this.getMainNode()
                .setFlags(GridFlags.DENSE_CAPACITY)
                .setTagName("wireless_transceiver_node")
                .setVisualRepresentation(this.getItemFromBlockEntity());
    }

    @Override
    public void updateStatus(ClusterAssemblerMatrix cluster) {
        super.updateStatus(cluster);
        if (cluster instanceof HelperAssemblerMatrixModifier clusterHelper)
            clusterHelper.eaep$markUploadCore();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.linkSlave.onUnloadOrRemove();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.linkSlave.onUnloadOrRemove();
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player) {
        super.exportSettings(mode, builder, player);

        if (mode != SettingsFrom.MEMORY_CARD) return;
        builder.set(ModDataComponents.DATA_PORT_UPLOAD_SETTINGS, new DataSettings(
                this.host.getLabel(),
                this.host.getPlacer(),
                this.host.getPlacerName()
        ));
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (this.isClientSide()) return;
        if (!input.has(ModDataComponents.DATA_PORT_UPLOAD_SETTINGS.get())) return;
        var settings = input.get(ModDataComponents.DATA_PORT_UPLOAD_SETTINGS.get());

        this.host.setPlacer(settings.placer());
        this.host.setPlacerName(settings.placerName);
        this.setLabel(settings.label, true);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        GridHelper.onFirstTick(this, $ -> this.linkSlave.register());
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
        this.linkSlave.setPlacer(id);
    }

    public void setLabel(Label label, boolean force) {
        if (!force && this.getBlockState().getValue(BlockUpload.LOCKED)) return;
        this.linkSlave.setLabel(label);
        this.host.setPlacer(label.data.placer());
        this.host.setPlacerName(label.data.placerName());
    }

    public Label getLabel() {
        return this.host.getLabel();
    }

    public @Nullable UUID getPlacer() {
        return this.host.getPlacer();
    }

    public String getPlacerName() {
        return this.host.getPlacerName();
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
    public boolean isLocked() {
        return this.getBlockState().getValue(BlockUpload.LOCKED);
    }

    @Override
    public void toggleLock() {
        var next = !this.isLocked();
        var stateNext = this.getBlockState().setValue(BlockUpload.LOCKED, next);
        if (this.level == null) return;
        this.level.setBlock(this.getBlockPos(), stateNext, Block.UPDATE_CLIENTS);
    }

    public record DataSettings(Label label, @Nullable UUID placer, String placerName) {
        public static final Codec<DataSettings> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Label.Data.CODEC.fieldOf("data_label").forGetter(data -> data.label.data),
                UUIDUtil.CODEC.lenientOptionalFieldOf("placer").forGetter(data -> Optional.ofNullable(data.placer())),
                Codec.STRING.fieldOf("placer_name").forGetter(DataSettings::placerName)
        ).apply(inst, (data, placer, placerName) ->
                new DataSettings(data.pack(), placer.orElse(null), placerName)));

        public static final StreamCodec<RegistryFriendlyByteBuf, DataSettings> STREAM_CODEC = StreamCodec.composite(
                Label.Data.STREAM_CODEC, data -> data.label.data,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.placer()),
                ByteBufCodecs.STRING_UTF8, DataSettings::placerName,
                (data, placer, placerName) ->
                        new DataSettings(data.pack(), placer.orElse(null), placerName)
        );
    }
}
