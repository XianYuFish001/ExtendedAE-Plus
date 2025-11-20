package com.extendedae_plus.integration.recipeViewer.emi;

import com.extendedae_plus.mixin.impl.ExclusionZoneScalingButton;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class EAEPEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea(new ExclusionZoneScalingButton());
    }
}
