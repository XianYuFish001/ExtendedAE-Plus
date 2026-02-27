package com.extendedae_plus.integration.impl.bean

import com.extendedae_plus.integration.impl.point.IntegrationAppliedFlux
import com.fish.fishlib.integration.BeanIntegration
import com.glodblock.github.appflux.common.me.key.FluxKey
import com.glodblock.github.appflux.common.me.key.type.EnergyType

@BeanIntegration("appflux")
object ImplAppliedFlux : IntegrationAppliedFlux {
    override val keyFlux
        get() = FluxKey.of(EnergyType.FE)!!
}