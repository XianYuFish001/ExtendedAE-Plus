package com.extendedae_plus.client.render.widgets.button

import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToServer
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

class EAEPServerCycleButton(
    states: List<EAEPActionItems>,
    taskSingle: (EAEPActionItems) -> Unit,
    iteratorState: IteratorState?,
    private val syncerState: () -> Int
) : EAEPCycleButton(
    states,
    { _, action -> taskSingle(action) },
    iteratorState
) {
    override fun iterateState(reversed: Boolean): Int {
        super.iterateState(reversed)
        val synced = this.syncerState()
        if (this.indexState != synced)
            this.indexState = synced
        return this.indexState
    }

    fun updateState() = this.setStateIndex(this.syncerState())

    class Builder {
        private val states = ArrayList<EAEPActionItems>()
        private var task = { _: EAEPActionItems -> }
        private var iteratorState: IteratorState? = null
        private var syncerState: (() -> Int)? = null

        fun setTask(task: CustomPacketPayload) =
            this.setTask { _ -> task.sendToServer() }

        fun setTask(task: Runnable) =
            this.setTask { _ -> task.run() }

        fun setTask(task: (EAEPActionItems) -> Unit): Builder {
            this.task = task
            return this
        }

        fun addPart(action: EAEPActionItems): Builder {
            this.states += action
            return this
        }

        fun setIterator(iteratorState: IteratorState?): Builder {
            this.iteratorState = iteratorState
            return this
        }

        fun setSyncer(syncer: () -> Int): Builder {
            this.syncerState = syncer
            return this
        }

//        fun setSyncer(syncer: SyncerBooleanGeneric): Builder {
//            this.syncerState = syncer
//            return this
//        }
//
//        fun <TEnum : Enum<TEnum>> setSyncer(syncer: SyncerEnumGeneric<TEnum>): Builder {
//            this.syncerState = syncer
//            return this
//        }

        fun build() = EAEPServerCycleButton(
            this.states,
            this.task,
            this.iteratorState,
            this.syncerState
                ?: throw IllegalArgumentException("Cannot build button without setSyncer call")
        )
    }

}

fun interface SyncerEnumGeneric<TEnum : Enum<TEnum>> : (() -> Int) {
    fun get(): TEnum

    override operator fun invoke() = this.get().ordinal
}

fun interface SyncerBooleanGeneric : (() -> Int) {
    fun get(): Boolean

    override operator fun invoke() = if (this.get()) 1 else 0
}