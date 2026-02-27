package com.extendedae_plus.mixin.helper

import com.extendedae_plus.common.registry.settings.StateSmartBlocking

interface SyncerSmartBlocking {
    fun `eaep$getBlockingState`(): StateSmartBlocking
}
