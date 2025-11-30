package com.extendedae_plus.common.init;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.client.gui.Icon;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.common.part.ticker.PartTicker;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModSettings {
    public static final Map<String, Setting<?>> EAEP_SETTINGS = new HashMap<>();
    public static final Map<Enum<?>, ButtonAppearance> appearances = new HashMap<>();

    public static final Setting<PartTicker.StateTicker> STATE_TICKER =
            register("state_ticker", PartTicker.StateTicker.class)
                    .bindAll(EAEPActionItems.TICKER_ENABLED,
                            EAEPActionItems.TICKER_DISABLED,
                            EAEPActionItems.TICKER_BLACKLISTED)
                    .setValidValue(PartTicker.StateTicker.ENABLED,
                            PartTicker.StateTicker.DISABLED)
                    .build();

    public static final Setting<RedstoneMode> OPTIONAL_REDSTONE_MODE =
            register("optional_redstone_mode", RedstoneMode.class)
                    .addPart(RedstoneMode.IGNORE, EAEPActionItems.REDSTONE_IGNORE)
                    .addPart(RedstoneMode.LOW_SIGNAL, EAEPActionItems.REDSTONE_LOW)
                    .addPart(RedstoneMode.HIGH_SIGNAL, EAEPActionItems.REDSTONE_HIGH)
                    .build();

    private static <TEnum extends Enum<TEnum>> Builder<TEnum> register(String name, Class<TEnum> clazzSetting) {
        return new Builder<>(name, clazzSetting);
    }

    private static class Builder<TEnum extends Enum<TEnum>> {
        private final String name;
        private final Class<TEnum> clazzSetting;
        private final List<Pair<TEnum, ButtonAppearance>> entryPairs = new ArrayList<>();
        private EnumSet<TEnum> validValues = null;

        public Builder(String name, Class<TEnum> clazzSetting) {
            this.name = name;
            this.clazzSetting = clazzSetting;
        }

        public Builder<TEnum> bindAll(EAEPActionItems... actions) {
            var values = EnumSet.allOf(this.clazzSetting);
            if (values.size() != actions.length)
                throw new IllegalArgumentException("Found unbound setting values");

            int index = 0;
            for (var value : values) {
                this.addPart(value, actions[index]);
                index++;
            }
            return this;
        }

        public Builder<TEnum> addPart(TEnum value, EAEPActionItems action) {
            return this.addPart(value, action, null);
        }

        public Builder<TEnum> addPart(TEnum value, EAEPActionItems action, @Nullable Item item) {
            this.entryPairs.add(new Pair<>(value, new ButtonAppearance(action, item)));
            return this;
        }

        @SafeVarargs
        public final Builder<TEnum> setValidValue(TEnum... validValues) {
            this.validValues = EnumSet.noneOf(this.clazzSetting);
            this.validValues.addAll(Arrays.asList(validValues));
            return this;
        }

        public Setting<TEnum> build() {
            boolean flagValidValuesSet = this.validValues != null;
            var boundValues = flagValidValuesSet
                    ? null : EnumSet.noneOf(this.clazzSetting);

            this.entryPairs.forEach(entryPair -> {
                appearances.put(entryPair.getFirst(), entryPair.getSecond());

                if (flagValidValuesSet) return;
                boundValues.add(entryPair.getFirst());
            });

            var setting = new Setting<>(this.name, this.clazzSetting,
                    flagValidValuesSet ? this.validValues : boundValues);
            EAEP_SETTINGS.put(this.name, setting);

            return setting;
        }
    }

    public record ButtonAppearance(EAEPActionItems action, @Nullable Item item) {
        public Icon icon() {
            return this.action.getAEIcon();
        }

        public List<Component> tooltipLines() {
            return List.of(this.action.getName(), this.action.getTooltip());
        }
    }
}
