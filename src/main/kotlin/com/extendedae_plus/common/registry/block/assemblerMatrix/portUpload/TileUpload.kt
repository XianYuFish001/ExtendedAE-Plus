package com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload

import appeng.api.networking.GridFlags
import appeng.api.networking.GridHelper
import appeng.util.SettingsFrom
import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.init.EAEPTiles
import com.extendedae_plus.common.registry.menu.host.link.HostLabelLink
import com.extendedae_plus.common.wireless.LinkSlave
import com.extendedae_plus.common.wireless.host.HostGeneric
import com.extendedae_plus.common.wireless.linkApi.IBlockEntityLabel
import com.extendedae_plus.common.wireless.linkApi.Label
import com.extendedae_plus.mixin.helper.BridgeMatrixFunctionExternal
import com.extendedae_plus.mixin.helper.HelperAssemblerMatrixModifier
import com.fish.fishlib.util.extension.invoke
import com.fish.fishlib.util.extension.optional
import com.fish.fishlib.util.property
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixWall
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.UUIDUtil
import net.minecraft.core.component.DataComponentMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import kotlin.jvm.optionals.getOrNull

class TileUpload(
    pos: BlockPos,
    blockState: BlockState
) : TileAssemblerMatrixWall(
    EAEPTiles.PortUpload(),
    pos,
    blockState
), IBlockEntityLabel, HostLabelLink, BridgeMatrixFunctionExternal {
    private val host = HostGeneric(
        this::getBlockEntity,
        this.mainNode::getNode
    )
    private val linkSlave = LinkSlave(this.host)

    var locked by this.property(BlockUpload.PropertyLocked, false)

    init {
        this.mainNode
            .setFlags(GridFlags.DENSE_CAPACITY)
            .setTagName("wireless_transceiver_node")
            .setVisualRepresentation(this.itemFromBlockEntity)
    }

    override fun add(cluster: HelperAssemblerMatrixModifier) = cluster.`eaep$markUploadCore`()

    override fun onChunkUnloaded() {
        super.onChunkUnloaded()
        this.linkSlave.onUnloadOrRemove()
    }

    override fun setRemoved() {
        super.setRemoved()
        this.linkSlave.onUnloadOrRemove()
    }

    override fun exportSettings(mode: SettingsFrom, builder: DataComponentMap.Builder, player: Player?) {
        super.exportSettings(mode, builder, player)

        if (mode != SettingsFrom.MEMORY_CARD) return
        builder.set(
            EAEPDataComponents.SettingsPortUpload, DataSettings(
                this.host.label,
                this.host.placer,
                this.host.placerName
            )
        )
    }

    override fun importSettings(mode: SettingsFrom, input: DataComponentMap, player: Player?) {
        super.importSettings(mode, input, player)

        if (this.isClientSide()) return
        if (!input.has(EAEPDataComponents.SettingsPortUpload())) return
        val settings = input.get(EAEPDataComponents.SettingsPortUpload())

        this.host.placer = settings!!.placer
        this.host.placerName = settings.placerName
        this.setLabel(settings.label, true)
    }

    override fun onLoad() {
        super.onLoad()
        GridHelper.onFirstTick(this) { it.linkSlave.register() }
    }

    override fun loadTag(data: CompoundTag, registries: HolderLookup.Provider) {
        super.loadTag(data, registries)

        DataSettings.codec.parse(NbtOps.INSTANCE, data.get("data_settings"))
            .resultOrPartial(Logger::error)
            .ifPresent { settings ->
                this.setLabel(settings.label, true)
                this.host.placer = settings.placer
                this.host.placerName = settings.placerName
            }
    }

    override fun saveAdditional(data: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(data, registries)
        DataSettings.codec.encodeStart(
            NbtOps.INSTANCE, DataSettings(
                this.label,
                this.placer,
                this.placerName
            )
        ).resultOrPartial(Logger::error)
            .ifPresent { data.put("data_settings", it) }
    }

    override fun setOwner(owner: Player) {
        super.setOwner(owner)
        val id = owner.getUUID()
        val name = owner.name.string
        this.host.placerName = name
        this.linkSlave.placer = id
    }

    override fun setLabel(label: Label, force: Boolean) {
        if (!force && this.locked) return
        this.linkSlave.label = label
        this.host.placer = label.data.placer
        this.host.placerName = label.data.placerName
    }

    override val label get() = this.host.label

    val placer get() = this.host.placer

    val placerName get() = this.host.placerName

    override val labelData = this.label.data

    override fun setLabelData(label: Label.Data, force: Boolean): Boolean {
        this.setLabel(label.pack(), force)
        return this.labelData == label
    }

    override val isLockable = true

    override val isLocked by this::locked

    override fun toggleLock() {
        this.locked = !this.locked
    }

    @JvmRecord
    data class DataSettings(val label: Label, val placer: UUID?, val placerName: String) {
        companion object {
            val codec: Codec<DataSettings> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Label.Data.codec.fieldOf("data_label").forGetter { it.label.data },
                    UUIDUtil.CODEC.lenientOptionalFieldOf("placer").forGetter(DataSettings::placer.optional()),
                    Codec.STRING.fieldOf("placer_name").forGetter(DataSettings::placerName)
                ).apply(instance) { data, placer, placerName ->
                    DataSettings(
                        data.pack(),
                        placer.getOrNull(),
                        placerName
                    )
                }
            }

            val streamCodec: StreamCodec<RegistryFriendlyByteBuf, DataSettings> = StreamCodec.composite(
                Label.Data.streamCodec, { it.label.data },
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), DataSettings::placer.optional(),
                ByteBufCodecs.STRING_UTF8, DataSettings::placerName
            ) { data, placer, placerName ->
                DataSettings(
                    data.pack(),
                    placer.getOrNull(),
                    placerName
                )
            }
        }
    }

    companion object {
        private val Logger = LogUtils.getLogger()
    }
}
