package com.extendedae_plus.client.render.widgets.button

import appeng.client.gui.AEBaseScreen
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.PacketDistributor

open class EAEPCycleButton(
    protected val states: List<EAEPActionItems>,
    statedOnPress: (Int, EAEPActionItems) -> Unit,
    protected val iteratorState: IteratorState?
) : EAEPButton(onPress@{ button ->
    if (button !is EAEPCycleButton) return@onPress
    val right = (Minecraft.getInstance().screen as? AEBaseScreen<*>)
        ?.isHandlingRightClick
        ?: false
    statedOnPress(
        button.iterateState(right),
        button.action ?: return@onPress
    )
}) {
    @JvmField
    protected var indexState: Int = 0

    init {
        this.updateTooltip()
    }

    override val action: EAEPActionItems?
        get() = this.states[this.indexState]

    fun setStateIndex(stateIndex: Int) {
        this.setStateIndex(stateIndex, false)
    }

    fun setStateIndex(stateIndex: Int, triggerEvent: Boolean) {
        this.indexState = stateIndex
        if (triggerEvent) this.onPress()
        this.updateTooltip()
    }

    /**
     * @return 被迭代过的stateIndex
     */
    open fun iterateState(reversed: Boolean): Int {
        this.indexState = if (this.iteratorState != null) {
            this.iteratorState.iterate(this.indexState, reversed)
        } else {
            val size = this.states.size
            (this.indexState + (if (reversed) -1 else 1) + size) % size
        }
        return this.indexState
    }

    class Builder {
        private val states = ArrayList<EAEPActionItems>()
        private val tasks = ArrayList<(EAEPActionItems) -> Unit>()
        private var task = { _: EAEPActionItems -> }
        private var iteratorState: IteratorState? = null

        fun addPart(action: EAEPActionItems, packet: CustomPacketPayload) =
            this.addPart(action) { -> PacketDistributor.sendToServer(packet) }

        fun addPart(action: EAEPActionItems, onPress: () -> Unit) =
            this.addPart(action) { _ -> onPress() }

        @JvmOverloads
        fun addPart(
            action: EAEPActionItems,
            onPress: (EAEPActionItems) -> Unit = { }
        ): Builder {
            this.states += action
            this.tasks += onPress
            return this
        }

        fun globalTask(task: (EAEPActionItems) -> Unit): Builder {
            this.task = task
            return this
        }

        fun setIterator(iteratorState: IteratorState?): Builder {
            this.iteratorState = iteratorState
            return this
        }

        fun build() = EAEPCycleButton(
            this.states,
            { index, action ->
                this.task(action)
                this.tasks[index](action)
            }, this.iteratorState
        )
    }

    fun interface IteratorState {
        fun iterate(prev: Int, reversed: Boolean): Int
    }
}
