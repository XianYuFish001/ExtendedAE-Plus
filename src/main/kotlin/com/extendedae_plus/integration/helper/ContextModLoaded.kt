package com.extendedae_plus.integration.helper

import com.fish.fishlib.common.InitObject
import net.neoforged.fml.ModList

enum class ContextModLoaded(
    private val modID: String,
    private val required: Boolean? = null
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
    ExpandedAE("expandedae", false),
    ;

    var loaded = false
        private set

    operator fun invoke() = this.loaded

    fun shouldTip() = this.required != null && this.required != this.loaded

    companion object {
        @InitObject(priority = 0)
        private fun init() {
            for (context in entries) {
                context.loaded = ModList.get().isLoaded(context.modID)
            }
        }
    }
}
