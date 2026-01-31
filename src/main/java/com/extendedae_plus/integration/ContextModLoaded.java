package com.extendedae_plus.integration;

import com.extendedae_plus.common.init.InitObject;
import lombok.Getter;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

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
    expandedAE("expandedae", false),

    ;

    private final String modID;
    @Getter
    private boolean loaded;
    private final @Nullable Boolean required;

    ContextModLoaded(String modID) {
        this(modID, null);
    }

    ContextModLoaded(String modID, @Nullable Boolean required) {
        this.modID = modID;
        this.loaded = false;
        this.required = required;
    }

    boolean shouldTip() {
        return this.required != null && this.required != this.loaded;
    }

    @InitObject(priority = 0)
    private static void init() {
        for (ContextModLoaded context : ContextModLoaded.values()) {
            context.loaded = ModList.get().isLoaded(context.modID);
        }
    }
}
