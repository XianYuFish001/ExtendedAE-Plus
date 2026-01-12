package com.extendedae_plus.integration;

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

    private static boolean initialized;

    private final String modID;
    private boolean loaded;

    ContextModLoaded(String modID) {
        this.modID = modID;
        this.loaded = false;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public static void init() {
        if (initialized)
            throw new IllegalStateException("Contexts has already been initialized");
        initialized = true;

        for (ContextModLoaded context : ContextModLoaded.values()) {
            context.loaded = ModList.get().isLoaded(context.modID);
        }
    }
}
