package com.extendedae_plus.mixin.helper

import appeng.api.config.YesNo

interface SyncerSmartDoubling {
    fun `eaep$getDoublingState`(): YesNo
}
