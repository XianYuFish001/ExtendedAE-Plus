package com.extendedae_plus.common.impl.pattern.smartDoubling

import java.util.*

/**
 * Thread-local stack holder for requested amounts to support nested requests.
 */
object HolderCraftingAmount {
    private val HOLDER: ThreadLocal<Deque<Long>> = ThreadLocal.withInitial { ArrayDeque() }

    /**
     * Push a requested amount onto the thread-local stack.
     */
    @JvmStatic
    fun push(v: Long) {
        val dq = HOLDER.get()
        dq.push(v)
    }

    /**
     * Pop the top value from the thread-local stack. Safe if empty.
     */
    @JvmStatic
    fun pop() {
        val dq = HOLDER.get()
        if (dq.isEmpty()) {
            return
        }
        dq.pop()
    }

    /**
     * Peek the current requested amount or return 0 if none.
     */
    @JvmStatic
    fun get(): Long {
        val dq = HOLDER.get()
        val v = dq.peek()
        return v ?: 0L
    }
}


