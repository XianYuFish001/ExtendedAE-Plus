package com.extendedae_plus.integration.impl.point

import appeng.api.stacks.AEKey

interface IntegrationAppliedMek {
    fun isMekKey(key: AEKey): Boolean

    fun getStack(key: AEKey): Any?

    val TypeChemicalJei: () -> Class<out Any>
}