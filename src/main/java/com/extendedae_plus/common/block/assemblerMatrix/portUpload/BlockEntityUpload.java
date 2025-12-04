package com.extendedae_plus.common.block.assemblerMatrix.portUpload;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.util.SettingsFrom;
import com.extendedae_plus.common.api.IBlockEntityFrequency;
import com.extendedae_plus.common.init.ModBlockEntities;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.wireless.LinkSlave;
import com.extendedae_plus.common.wireless.host.HostGeneric;
import com.extendedae_plus.common.wireless.linkApi.LinkRegistry;
import com.extendedae_plus.mixin.impl.bridge.HelperAssemblerMatrixModifier;
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixWall;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BlockEntityUpload extends TileAssemblerMatrixWall implements IBlockEntityFrequency {
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
                this.host.getFrequency(),
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
        this.setFrequency(settings.frequency, false);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        GridHelper.onFirstTick(this, blockEntity -> this.linkSlave.register());
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
        this.linkSlave.setPlacer(id);
    }

    public void setFrequency(long frequency, boolean force) {
        if (!force && this.getBlockState().getValue(BlockUpload.LOCKED)) return;
        this.linkSlave.setFrequency(frequency);
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

    public record DataSettings(long frequency, @Nullable UUID placer, String placerName) {
        public static final Codec<DataSettings> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.LONG.fieldOf("frequency").forGetter(DataSettings::frequency),
                UUIDUtil.CODEC.lenientOptionalFieldOf("placer").forGetter(data -> Optional.ofNullable(data.placer())),
                Codec.STRING.fieldOf("placer_name").forGetter(DataSettings::placerName)
        ).apply(inst, (frequency, placer, placerName) ->
                new DataSettings(frequency, placer.orElse(null), placerName)));

        public static final StreamCodec<RegistryFriendlyByteBuf, DataSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, DataSettings::frequency,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.placer()),
                ByteBufCodecs.STRING_UTF8, DataSettings::placerName,
                (frequency, placer, placerName) ->
                        new DataSettings(frequency, placer.orElse(null), placerName)
        );
    }
}
