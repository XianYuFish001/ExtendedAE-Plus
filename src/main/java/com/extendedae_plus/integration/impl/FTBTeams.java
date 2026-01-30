package com.extendedae_plus.integration.impl;

import com.extendedae_plus.integration.ContextModLoaded;
import com.extendedae_plus.integration.IntegrationFTBTeams;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class FTBTeams implements IntegrationFTBTeams {
    public Optional<UUID> getTeamUUID(@Nullable UUID member) {
        if (member == null || !ContextModLoaded.ftbTeams.isLoaded())
            return Optional.empty();

        return FTBTeamsAPI.api().getManager().getTeamForPlayerID(member)
                .map(Team::getId);
    }

    public Component getTeamName(@Nullable UUID uuid) {
        if (uuid == null || !ContextModLoaded.ftbTeams.isLoaded())
            return Component.empty();

        var manager = FTBTeamsAPI.api().getManager();
        var team = manager.getTeamByID(uuid);
        if (team.isEmpty()) team = manager.getTeamForPlayerID(uuid);
        if (team.isEmpty()) return Component.empty();
        return team.get().getColoredName();
    }
}
