package com.extendedae_plus.common.registry.block.wirelessTransceiver

import appeng.api.networking.GridFlags
import appeng.api.networking.GridHelper
import appeng.blockentity.grid.AENetworkedBlockEntity
import appeng.util.SettingsFrom
import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.init.EAEPTiles
import com.extendedae_plus.common.registry.menu.host.link.HostLabelLink
import com.extendedae_plus.common.wireless.LinkMaster
import com.extendedae_plus.common.wireless.LinkSlave
import com.extendedae_plus.common.wireless.linkApi.IBlockEntityLabel
import com.extendedae_plus.common.wireless.linkApi.ILinkHost
import com.extendedae_plus.common.wireless.linkApi.Label
import com.fish.fishlib.util.extension.invoke
import com.fish.fishlib.util.extension.optional
import com.fish.fishlib.util.property
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
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import kotlin.jvm.optionals.getOrNull

class TileWirelessTransceiver(
    pos: BlockPos, blockState: BlockState
) : AENetworkedBlockEntity(
    EAEPTiles.WirelessTransceiver(),
    pos,
    blockState
), IBlockEntityLabel, HostLabelLink {
    private val host: LinkHostTransceiver

    private val linkMaster: LinkMaster
    private val linkSlave: LinkSlave

    var master by this.property(BlockWirelessTransceiver.PropertyMaster, false)
    var powered by this.property(BlockWirelessTransceiver.PropertyPowered, false)
    var locked by this.property(BlockWirelessTransceiver.PropertyLocked, false)

    init {
        this.host = LinkHostTransceiver()
        this.linkMaster = LinkMaster(this.host)
        this.linkSlave = LinkSlave(this.host)

        this.mainNode
            .setFlags(GridFlags.DENSE_CAPACITY)
            .setTagName("wireless_transceiver_node")
            .setVisualRepresentation(this.itemFromBlockEntity)
    }

    override fun onChunkUnloaded() {
        super.onChunkUnloaded()
        this.linkMaster.onUnloadOrRemove()
        this.linkSlave.onUnloadOrRemove()
    }

    override fun setRemoved() {
        super.setRemoved()
        this.linkMaster.onUnloadOrRemove()
        this.linkSlave.onUnloadOrRemove()
    }

    fun onSwitchMode() {
        this.linkMaster.unregister()
        this.linkSlave.unregister()

        if (this.master)
            this.linkMaster.register()
        else this.linkSlave.register()
    }

    override fun exportSettings(mode: SettingsFrom, builder: DataComponentMap.Builder, player: Player?) {
        super.exportSettings(mode, builder, player)

        if (mode != SettingsFrom.MEMORY_CARD) return
        builder.set(
            EAEPDataComponents.SettingsTransceiver, DataSettings(
                this.master,
                this.host.label,
                this.host.placer,
                this.host.placerName
            )
        )
    }

    override fun importSettings(mode: SettingsFrom, input: DataComponentMap, player: Player?) {
        super.importSettings(mode, input, player)

        if (this.level !is ServerLevel) return
        if (!input.has(EAEPDataComponents.SettingsTransceiver())) return
        val settings = input.get(EAEPDataComponents.SettingsTransceiver())

        if (settings!!.masterMode != this.master)
            this.master = settings.masterMode

        this.host.placer = settings.placer
        this.host.placerName = settings.placerName
        this.setLabel(settings.label, true)
    }

    override fun onLoad() {
        super.onLoad()

        GridHelper.onFirstTick(this) { tile ->
            if (tile.master) tile.linkMaster.register()
            else tile.linkSlave.register()
        }
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
                false,
                this.label,
                this.placer,
                this.placerName
            )
        ).resultOrPartial(Logger::error)
            .ifPresent { data.put("data_settings", it) }
    }

    override fun setOwner(owner: Player) {
        super.setOwner(owner)
        val id = owner.uuid
        val name = owner.name.string
        this.host.placerName = name
        this.linkMaster.setPlacer(id)
        this.linkSlave.placer = id
    }

    override fun setLabel(label: Label, force: Boolean) {
        if (!force && this.locked) return
        if (this.master)
            this.linkMaster.label = label
        else this.linkSlave.label = label
        this.host.placer = label.data.placer
        this.host.placerName = label.data.placerName
    }

    override val label get() = this.host.label

    override val labelData get() = this.label.data

    override fun setLabelData(label: Label.Data, force: Boolean): Boolean {
        this.setLabel(label.pack(), force)
        return this.labelData == label
    }

    override val isLockable = true

    override val isMasterable = true

    override val isLocked by this::locked

    override val isMaster by this::master

    override fun toggleLock() {
        this.locked = !this.locked
    }

    override fun toggleMaster() {
        this.master = !this.master
        this.onSwitchMode()
    }

    val placer get() = this.host.placer

    val placerName get() = this.host.placerName

    private inner class LinkHostTransceiver : ILinkHost {
        override var label: Label = Label.Empty
        override var placer: UUID? = null
        override var placerName = ""

        override val serverLevel get() = this@TileWirelessTransceiver.level as? ServerLevel
        override val blockPos: BlockPos get() = this@TileWirelessTransceiver.worldPosition
        override val gridNode get() = this@TileWirelessTransceiver.gridNode
        override val isRemoved get() = this@TileWirelessTransceiver.isRemoved

        override fun onConnectionChanged(connected: Boolean) {
            if (this.serverLevel == null) return
            if (!this.isRemoved && this@TileWirelessTransceiver.powered != connected
            ) this@TileWirelessTransceiver.powered = connected
        }
    }

    @JvmRecord
    data class DataSettings(val masterMode: Boolean, val label: Label, val placer: UUID?, val placerName: String) {
        companion object {
            val codec: Codec<DataSettings> =
                RecordCodecBuilder.create { instance ->
                    instance.group(
                        Codec.BOOL.fieldOf("master_mode").forGetter(DataSettings::masterMode),
                        Label.Data.codec.fieldOf("data_label").forGetter { it.label.data },
                        UUIDUtil.CODEC.lenientOptionalFieldOf("placer").forGetter(DataSettings::placer.optional()),
                        Codec.STRING.fieldOf("placer_name").forGetter(DataSettings::placerName)
                    ).apply(
                        instance
                    ) { masterMode, data, placer, placerName ->
                        DataSettings(
                            masterMode,
                            data.pack(),
                            placer.getOrNull(),
                            placerName
                        )
                    }
                }

            val streamCodec: StreamCodec<RegistryFriendlyByteBuf, DataSettings> = StreamCodec.composite(
                ByteBufCodecs.BOOL, DataSettings::masterMode,
                Label.Data.streamCodec, { it.label.data },
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), DataSettings::placer.optional(),
                ByteBufCodecs.STRING_UTF8, DataSettings::placerName,
                { masterMode, data, placer, placerName ->
                    DataSettings(
                        masterMode,
                        data.pack(),
                        placer.getOrNull(),
                        placerName
                    )
                }
            )
        }
    }

    companion object {
        private val Logger = LogUtils.getLogger()
    }
}
