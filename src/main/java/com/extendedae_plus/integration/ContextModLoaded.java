package com.extendedae_plus.integration;

import com.extendedae_plus.common.init.InitObject;
import net.neoforged.fml.ModList;

public enum ContextModLoaded {
    emi("emi"),
    jei("jei"),
    ftbTeams("ftbteams"),
    jech("jecharacters"),
    curios("curios"),
    ae2wtlib("ae2wtlib"),
    advancedAE("advanced_ae"),
    appliedFlux("appflux"),
    mekanism("mekanism"),
    appliedMekanistics("appmek"),
    gtceuModern("gtceu"),

    ;

    private final String modID;
    private boolean loaded;

    ContextModLoaded(String modID) {
        this.modID = modID;
        this.loaded = false;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    @InitObject(priority = 0)
    private static void init() {
        for (ContextModLoaded context : ContextModLoaded.values()) {
            context.loaded = ModList.get().isLoaded(context.modID);
        }
    }
}
