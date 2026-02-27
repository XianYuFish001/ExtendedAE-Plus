package com.extendedae_plus.common.registry.menu.labelLink

import appeng.menu.AEBaseMenu
import appeng.menu.guisync.GuiSync
import appeng.menu.implementations.MenuTypeBuilder
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.extendedae_plus.common.registry.menu.host.link.HostLabelLink
import com.extendedae_plus.common.wireless.linkApi.Label
import com.extendedae_plus.common.wireless.linkApi.RegistryLink
import com.extendedae_plus.network.SPacketLabelList
import com.fish.fishlib.util.extension.invoke
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.MenuType

class MenuLabelLink(
    menuType: MenuType<*>,
    id: Int,
    playerInventory: Inventory,
    private val host: HostLabelLink
) : AEBaseMenu(menuType, id, playerInventory, host) {
    @GuiSync(101)
    var selectedLabel: Label.Data
        private set

    @GuiSync(102)
    var isLocked: Boolean
        private set

    @GuiSync(103)
    var isMaster: Boolean
        private set

    // Server
    private lateinit var labels: MutableList<LabelMapped>
    private var initDelay = 3

    // Client
    var isLockable: Boolean = false
    var isMasterable: Boolean = false

    constructor(id: Int, playerInventory: Inventory, host: HostLabelLink) : this(id, playerInventory, host, false)

    constructor(
        id: Int,
        playerInv: Inventory,
        host: HostLabelLink,
        manageable: Boolean
    ) : this(
        if (manageable)
            EAEPMenuTypes.LabelLinkManageable()
        else
            EAEPMenuTypes.LabelLink(),
        id,
        playerInv,
        host
    )

    override fun broadcastChanges() {
        super.broadcastChanges()

        if (this.isClientSide) return

        if (this.initDelay == 0)
            SPacketLabelList.send(this)
        if (this.initDelay >= 0)
            initDelay--
    }

    init {
        this.selectedLabel = host.labelData
        this.isLocked = host.isLocked
        this.isMaster = host.isMaster

        this.registerClientAction(
            ACTION_SELECT,
            Int::class.javaObjectType,
            this::selectLabel
        )
        this.registerClientAction(
            ACTION_ADD,
            Label.Data::class.java,
            this::registerLabel
        )
        this.registerClientAction(
            ACTION_REMOVE,
            Int::class.javaObjectType,
            this::unregisterLabel
        )
        this.registerClientAction(ACTION_LOCK, this::toggleLock)
        this.registerClientAction(ACTION_MASTER, this::toggleMaster)
    }

    fun selectLabel(serial: Int) {
        if (this.isClientSide) {
            this.sendClientAction(ACTION_SELECT, serial)
            return
        }

        this.labels
            .find { it.serial == serial }
            ?.data?.let {
                if (this.host.setLabelData(it, false))
                    this.selectedLabel = it
            }
        this.player.closeContainer()
    }

    fun registerLabel(data: Label.Data) {
        if (this.isClientSide) {
            this.sendClientAction(ACTION_ADD, data)
            return
        }

        val data = data.convertPlacer()
        data.pack()
        if (this.host.setLabelData(data, false))
            this.selectedLabel = data
        this.initDelay = 0
    }

    fun unregisterLabel(serial: Int) {
        if (this.isClientSide) {
            this.sendClientAction(ACTION_REMOVE, serial)
            return
        }

        this.labels
            .find { it.serial == serial }
            ?.data?.let {
                if (it == this.host.labelData) {
                    this.host.setLabelData(Label.Data.Empty, true)
                    this.selectedLabel = Label.Data.Empty
                }
                RegistryLink.removeLabel(it)
                this.initDelay = 0
            }
    }

    fun toggleLock() {
        if (this.isClientSide) {
            if (!this.isLockable) return
            this.sendClientAction(ACTION_LOCK)
            return
        }
        this.host.toggleLock()
        this.isLocked = this.host.isLocked
    }

    fun toggleMaster() {
        if (this.isClientSide) {
            if (!this.isMasterable) return
            this.sendClientAction(ACTION_MASTER)
            return
        }
        this.host.toggleMaster()
        this.isMaster = this.host.isMaster
    }

    fun setLabels(labels: MutableList<LabelMapped>) {
        this.labels = labels
    }

    @JvmRecord
    data class LabelMapped(val serial: Int, val data: Label.Data) {
        companion object {
            val streamCodec: StreamCodec<RegistryFriendlyByteBuf, LabelMapped> = StreamCodec.composite(
                ByteBufCodecs.INT, LabelMapped::serial,
                Label.Data.streamCodec, LabelMapped::data,
                ::LabelMapped
            )
        }
    }

    companion object {
        val DataManagementSerializer: (MenuTypeBuilder<MenuLabelLink, HostLabelLink>) -> Unit = {
            it.withInitialData({ _, _ -> }) { host, menu, _ ->
                menu.isLockable = host.isLockable
                menu.isMasterable = host.isMasterable
            }
        }

        private const val ACTION_SELECT = "select"
        private const val ACTION_ADD = "add"
        private const val ACTION_REMOVE = "remove"
        private const val ACTION_LOCK = "lock"
        private const val ACTION_MASTER = "master"
    }
}
