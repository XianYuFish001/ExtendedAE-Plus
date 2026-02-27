package com.extendedae_plus.integration.impl.point

import net.minecraft.network.chat.Component
import java.util.*

interface IntegrationFTBTeams {
    fun getTeamUUID(member: UUID?): UUID?

    /**
     * @param uuid Team / Member
     */
    fun getTeamName(uuid: UUID?): Component
}