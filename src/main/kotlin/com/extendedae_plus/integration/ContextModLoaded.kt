package com.extendedae_plus.integration

import com.extendedae_plus.common.init.InitObject
import lombok.Getter
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

    @Getter
    private var loaded = false

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
