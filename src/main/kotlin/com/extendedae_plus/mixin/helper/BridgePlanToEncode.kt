package com.extendedae_plus.mixin.helper

interface BridgePlanToEncode {
    fun `eaep$plan`()

    fun `eaep$execute`()

    fun `eaep$planned`(): Boolean
}
