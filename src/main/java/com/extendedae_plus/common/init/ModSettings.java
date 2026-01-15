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
    public static final Map<String, Setting<?>> EAEP_SETTINGS = new HashMap<>();
    private static final Map<ValueEntry, ButtonAppearance> appearances = new HashMap<>();

    public static final Setting<PartTicker.StateTicker> STATE_TICKER =
            register("state_ticker", PartTicker.StateTicker.class)
                    .bindAll(EAEPActionItems.TICKER_ENABLED,
                            EAEPActionItems.TICKER_DISABLED,
                            EAEPActionItems.TICKER_BLACKLISTED)
                    .setInvalidValue(PartTicker.StateTicker.BLACKLISTED)
                    .build();
    public static final Setting<RedstoneMode> OPTIONAL_REDSTONE_MODE =
            register("optional_redstone_mode", RedstoneMode.class)
                    .addPart(RedstoneMode.IGNORE, EAEPActionItems.REDSTONE_IGNORE)
                    .addPart(RedstoneMode.LOW_SIGNAL, EAEPActionItems.REDSTONE_LOW)
                    .addPart(RedstoneMode.HIGH_SIGNAL, EAEPActionItems.REDSTONE_HIGH)
                    .build();
    public static final Setting<StateSmartBlocking> SMART_BLOCKING =
            register("smart_blocking", StateSmartBlocking.class)
                    .bindAll(EAEPActionItems.BLOCKING_ENABLED,
                            EAEPActionItems.BLOCKING_DISABLED,
                            EAEPActionItems.BLOCKING_DISABLED_BY_SUPER)
                    .setInvalidValue(StateSmartBlocking.DISABLED_BY_SUPER)
                    .build();
    public static final Setting<YesNo> SMART_DOUBLING =
            register("smart_doubling", YesNo.class)
                    .addPart(YesNo.YES, EAEPActionItems.DOUBLING_ENABLED)
                    .addPart(YesNo.NO, EAEPActionItems.DOUBLING_DISABLED)
                    .build();
    public static final Setting<YesNo> LABEL_LOCKED =
            register("label_locked", YesNo.class)
                    .addPart(YesNo.YES, EAEPActionItems.LABEL_LOCKED)
                    .addPart(YesNo.NO, EAEPActionItems.LABEL_UNLOCKED)
                    .build();
    public static final Setting<YesNo> TRANSCEIVER_MODE =
            register("transceiver_mode", YesNo.class)
                    .addPart(YesNo.YES, EAEPActionItems.TRANSCEIVER_MASTER)
                    .addPart(YesNo.NO, EAEPActionItems.TRANSCEIVER_SLAVE)
                    .build();
    public static final Setting<ModeEncodingTransfer> TRANSFER_MODE =
            register("transfer_mode", ModeEncodingTransfer.class)
                    .bindAll(EAEPActionItems.MERGE_NONE,
                            EAEPActionItems.MERGE_ADJACENCY,
                            EAEPActionItems.MERGE_INDEPENDENCE)
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
            return this.setting.hashCode() ^ this.value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final ValueEntry other = (ValueEntry) obj;
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
            EAEP_SETTINGS.put(this.name, setting);

            return setting;
        }
    }
}
