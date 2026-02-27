package com.extendedae_plus.integration.impl.recipeViewer.emi

import com.extendedae_plus.mixin.impl.ExclusionZoneScalingButton
import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry

@EmiEntrypoint
class EAEPEmiPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        registry.addGenericExclusionArea(ExclusionZoneScalingButton)
    }
}
