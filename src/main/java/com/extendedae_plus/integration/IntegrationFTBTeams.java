package com.extendedae_plus.integration;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationFTBTeams {
    IntegrationFTBTeams instance = getInstance();

    private static IntegrationFTBTeams getInstance() {
        var empty = new IntegrationFTBTeams() {
            @Override
            public Optional<UUID> getTeamUUID(@Nullable UUID member) {
                return Optional.empty();
            }

            @Override
            public Component getTeamName(@Nullable UUID uuid) {
                return Component.empty();
            }
        };

        if (!ContextModLoaded.ftbTeams.isLoaded())
            return empty;
        try {
            return (IntegrationFTBTeams) Class.forName("com.extendedae_plus.integration.instance.FTBTeams")
                    .getConstructor()
                    .newInstance();
        } catch (ClassNotFoundException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException e) {
            return empty;
        }
    }

    Optional<UUID> getTeamUUID(@Nullable UUID member);

    /// @param uuid Team / Member
    Component getTeamName(@Nullable UUID uuid);
}
