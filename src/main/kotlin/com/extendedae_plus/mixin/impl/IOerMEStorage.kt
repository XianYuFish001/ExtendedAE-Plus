package com.extendedae_plus.mixin.impl

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey

fun interface IOerMEStorage {
    fun apply(what: AEKey?, amount: Long, mode: Actionable?, source: IActionSource?): Long
}
