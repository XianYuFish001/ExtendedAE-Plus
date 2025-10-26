package com.extendedae_plus.integration.recipeViewer.emi;

import com.extendedae_plus.integration.recipeViewer.emi.emiIntegrations.EmiStackConverters;
import com.extendedae_plus.integration.recipeViewer.emi.emiIntegrations.converter.EmiFluidStackConverter;
import com.extendedae_plus.integration.recipeViewer.emi.emiIntegrations.converter.EmiItemStackConverter;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class EaepEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        EmiStackConverters.register(new EmiItemStackConverter());
        EmiStackConverters.register(new EmiFluidStackConverter());
    }
}
