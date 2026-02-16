package com.extendedae_plus.integration.instance;

import com.extendedae_plus.integration.ContextModLoaded;
import com.extendedae_plus.integration.IntegrationFTBTeams;
import com.mojang.datafixers.util.Either;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class FTBTeams implements IntegrationFTBTeams {
    public Optional<UUID> getTeamUUID(@Nullable UUID member) {
        if (member == null || !ContextModLoaded.ftbTeams.isLoaded())
            return Optional.empty();

        return ManagerWrapped.instance
                .getTeamForPlayerID(member)
                .map(Team::getId);
    }

    public Component getTeamName(@Nullable UUID uuid) {
        if (uuid == null || !ContextModLoaded.ftbTeams.isLoaded())
            return Component.empty();

        var manager = ManagerWrapped.instance;
        var team = manager.getTeamByID(uuid);
        if (team.isEmpty()) team = manager.getTeamForPlayerID(uuid);
        if (team.isEmpty()) return Component.empty();
        return team.get().getColoredName();
    }

    private enum ManagerWrapped {
        instance;

        private @Nullable Either<TeamManager, ClientTeamManager> manager;

        private Optional<Either<TeamManager, ClientTeamManager>> manager() {
            if (manager != null) return Optional.of(manager);
            var api = FTBTeamsAPI.api();
            manager = api.isManagerLoaded() ? Either.left(api.getManager())
                    : (api.isClientManagerLoaded() ? Either.right(api.getClientManager())
                    : null);
            return Optional.ofNullable(manager);
        }

        private Optional<Team> getTeamForPlayerID(@Nullable UUID member) {
            return this.manager().flatMap(it -> it.map(
                    left -> left.getTeamForPlayerID(member),
                    right -> Optional.empty()
            ));
        }

        private Optional<Team> getTeamByID(@Nullable UUID uuid) {
            return this.manager().flatMap(it -> it.map(
                    left -> left.getTeamByID(uuid),
                    right -> right.getTeamByID(uuid)
            ));
        }
    }
}
