package com.extendedae_plus.common.impl.pattern;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record InfoProvider(Component name, @Nullable AEItemKey icon, int serverID, int availableSlots) {
    public static final StreamCodec<RegistryFriendlyByteBuf, InfoProvider> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_STREAM_CODEC, InfoProvider::name,
            ByteBufCodecs.optional(AEKey.STREAM_CODEC), info -> Optional.ofNullable(info.icon),
            ByteBufCodecs.INT, InfoProvider::serverID,
            ByteBufCodecs.INT, InfoProvider::availableSlots,
            (name, icon, id, slots) ->
                    new InfoProvider(name, (AEItemKey) icon.orElse(null), id, slots)
    );

    public String i18nKey() {
        if (this.icon == null) return "";
        return this.icon.getId().toLanguageKey();
    }
}
