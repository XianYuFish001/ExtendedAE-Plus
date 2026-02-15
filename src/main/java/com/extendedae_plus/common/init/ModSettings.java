package com.extendedae_plus.common.init;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.YesNo;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.common.registry.part.ticker.PartTicker;
import com.extendedae_plus.common.registry.settings.ModeEncodingTransfer;
import com.extendedae_plus.common.registry.settings.StateSmartBlocking;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/// 使用EAEPCycleButton喵, 使用EAEPCycleButton谢谢喵
public class ModSettings {
    public static final Map<String, Setting<?>> settings = new HashMap<>();
    private static final Map<ValueEntry, ButtonAppearance> appearances = new HashMap<>();

    public static final Setting<PartTicker.StateTicker> stateTicker =
            register("state_ticker", PartTicker.StateTicker.class)
                    .bindAll(EAEPActionItems.tickerEnabled,
                            EAEPActionItems.tickerDisabled,
                            EAEPActionItems.tickerBlacklisted)
                    .setInvalidValue(PartTicker.StateTicker.BLACKLISTED)
                    .build();
    public static final Setting<RedstoneMode> modeRedstoneOptional =
            register("optional_redstone_mode", RedstoneMode.class)
                    .addPart(RedstoneMode.IGNORE, EAEPActionItems.redstoneIgnore)
                    .addPart(RedstoneMode.LOW_SIGNAL, EAEPActionItems.redstoneLow)
                    .addPart(RedstoneMode.HIGH_SIGNAL, EAEPActionItems.redstoneHigh)
                    .build();
    public static final Setting<StateSmartBlocking> smartBlocking =
            register("smart_blocking", StateSmartBlocking.class)
                    .bindAll(EAEPActionItems.blockingEnabled,
                            EAEPActionItems.blockingDisabled,
                            EAEPActionItems.blockingUnable)
                    .setInvalidValue(StateSmartBlocking.DISABLED_BY_SUPER)
                    .build();
    public static final Setting<YesNo> smartDoubling =
            register("smart_doubling", YesNo.class)
                    .addPart(YesNo.YES, EAEPActionItems.doublingEnabled)
                    .addPart(YesNo.NO, EAEPActionItems.doublingDisabled)
                    .build();
    public static final Setting<ModeEncodingTransfer> modeTransfer =
            register("transfer_mode", ModeEncodingTransfer.class)
                    .bindAll(EAEPActionItems.mergeNone,
                            EAEPActionItems.mergeAdjacency,
                            EAEPActionItems.mergeIndependence)
                    .build();

    private static <TEnum extends Enum<TEnum>> Builder<TEnum> register(String name, Class<TEnum> clazzSetting) {
        return new Builder<>(name, clazzSetting);
    }

    public static <TEnum extends Enum<TEnum>> ButtonAppearance findAppearance(Setting<TEnum> setting, TEnum value) {
        return appearances.get(new ValueEntry(setting.getName(), value));
    }

    public record ValueEntry(String setting, Enum<?> value) {
        @Override
        public int hashCode() {
            return Objects.hash(this.setting, this.value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (this.getClass() != obj.getClass()) return false;
            final var other = (ValueEntry) obj;
            return Objects.equals(other.setting, this.setting) && other.value == this.value;
        }
    }

    public record ButtonAppearance(EAEPActionItems action, @Nullable Item item) {
    }

    private static class Builder<TEnum extends Enum<TEnum>> {
        private final String name;
        private final Class<TEnum> clazzSetting;
        private final List<Pair<TEnum, ButtonAppearance>> entryPairs = new ArrayList<>();
        private EnumSet<TEnum> invalidValues = null;

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
        public final Builder<TEnum> setInvalidValue(TEnum... invalidValues) {
            this.invalidValues = EnumSet.noneOf(this.clazzSetting);
            Collections.addAll(this.invalidValues, invalidValues);
            return this;
        }

        public Setting<TEnum> build() {
            var boundValues = EnumSet.noneOf(this.clazzSetting);
            this.entryPairs.forEach(entryPair -> {
                appearances.put(new ValueEntry(this.name, entryPair.getFirst()), entryPair.getSecond());
                boundValues.add(entryPair.getFirst());
            });
            if (this.invalidValues != null)
                boundValues.removeAll(this.invalidValues);

            var setting = new Setting<>(this.name, this.clazzSetting, boundValues);
            settings.put(this.name, setting);

            return setting;
        }
    }
}
