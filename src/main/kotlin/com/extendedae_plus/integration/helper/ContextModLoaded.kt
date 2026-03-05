package com.extendedae_plus.integration.helper

import com.fish.fishlib.common.InitObject
import net.neoforged.fml.ModList

enum class ContextModLoaded(
    private val modID: String,
    private val flagDep: FlagDependency = FlagDependency.None
) {
    Emi("emi"),
    Jei("jei"),
    FTBTeams("ftbteams"),
    Jech("jecharacters"),
    Curios("curios"),
    AE2wtlib("ae2wtlib"),
    AdvancedAE("advanced_ae"),
    AppliedFlux("appflux"),
    Mekanism("mekanism"),
    AppliedMekanistics("appmek"),
    GtceuModern("gtceu"),
    ExpandedAE("expandedae", FlagDependency.Discouraged),
    ;

    var loaded = false
        private set

    operator fun invoke() = this.loaded

    fun shouldTip() = when (this.flagDep) {
        FlagDependency.None -> false
        FlagDependency.Required -> !this()
        FlagDependency.Discouraged, FlagDependency.Incompatible -> this()
    }

    companion object {
        @InitObject(0)
        private fun init() {
            entries.forEach {
                it.loaded = ModList.get().isLoaded(it.modID)
            }
        }
    }
}

enum class FlagDependency {
    Required, None, Discouraged, Incompatible
}
