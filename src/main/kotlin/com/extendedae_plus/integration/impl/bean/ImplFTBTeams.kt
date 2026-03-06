package com.extendedae_plus.integration.impl.bean

import com.extendedae_plus.integration.helper.ContextModLoaded
import com.extendedae_plus.integration.impl.point.IntegrationFTBTeams
import com.fish.fishlib.integration.BeanIntegration
import com.fish.fishlib.util.oneOf.OneOf2
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI
import dev.ftb.mods.ftbteams.api.TeamManager
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager
import net.minecraft.network.chat.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

@BeanIntegration("ftbteams")
object ImplFTBTeams : IntegrationFTBTeams {
    override fun getTeamUUID(member: UUID?): UUID? {
        if (member == null || !ContextModLoaded.FTBTeams())
            return null

        return ManagerWrapped
            .getTeamForPlayerID(member)
            ?.id
    }

    override fun getTeamName(uuid: UUID?): Component {
        if (uuid == null || !ContextModLoaded.FTBTeams()) return Component.empty()

        val team = ManagerWrapped.getTeamByID(uuid)
            ?: ManagerWrapped.getTeamForPlayerID(uuid)
            ?: return Component.empty()
        return team.coloredName
    }

    object ManagerWrapped {
        private val manager: OneOf2<TeamManager, ClientTeamManager> by lazy {
            val api = FTBTeamsAPI.api()
            when {
                api.isManagerLoaded -> OneOf2.a(api.manager)
                api.isClientManagerLoaded -> OneOf2.b(api.clientManager)
                else -> OneOf2.empty()
            }
        }

        fun getTeamForPlayerID(member: UUID) = this.manager.flatMap(
            { it.getTeamForPlayerID(member) },
            { null }
        )?.getOrNull()

        fun getTeamByID(uuid: UUID) = this.manager.flatMap(
            { it.getTeamByID(uuid) },
            { it.getTeamByID(uuid) }
        )?.getOrNull()
    }
}