package com.extendedae_plus;

import com.extendedae_plus.common.part.ticker.ParserTickerConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.regex.Pattern;

public final class EAEPConfig {
    static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.IntValue PAGE_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue INDEPENDENT_UPLOADING_BUTTON;

    static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_ENCODER_PATTERN_PLAYER;
    public static final ModConfigSpec.BooleanValue PATTERN_TERMINAL_SHOW_SLOTS_DEFAULT;
    public static final ModConfigSpec.BooleanValue ALLOW_DISK_ENERGY;
    public static final ModConfigSpec.BooleanValue OVERRIDE_AE2WT_PICKING;

    static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.BooleanValue NEEDS_UPLOADING_CORE;
    public static final ModConfigSpec.BooleanValue PROVIDER_ROUND_ROBIN_ENABLE;
    public static final ModConfigSpec.IntValue SMART_SCALING_MAX_MULTIPLIER;
    public static final ModConfigSpec.IntValue CRAFTING_PAUSE_THRESHOLD;
    public static final ModConfigSpec.DoubleValue WIRELESS_MAX_RANGE;
    public static final ModConfigSpec.BooleanValue WIRELESS_CROSS_DIM_ENABLE;
    public static final ModConfigSpec.IntValue TICKER_BASE_COST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TICKER_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TICKER_EXTERNAL_MULTIPLIER;

    static void init(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, EAEPConfig.COMMON_SPEC, "extendedae_plus/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, EAEPConfig.CLIENT_SPEC, "extendedae_plus/client.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, EAEPConfig.SERVER_SPEC, "extendedae_plus/server.toml");

        if (modContainer.getEventBus() == null)
            throw new IllegalStateException("EventBus is null");
        modContainer.getEventBus().addListener(ModConfigEvent.Loading.class, event -> reloadSetting(event.getConfig()));
        modContainer.getEventBus().addListener(ModConfigEvent.Reloading.class, event -> reloadSetting(event.getConfig()));
    }

    private static void reloadSetting(ModConfig config) {
        if (config.getSpec() == SERVER_SPEC) {
            ParserTickerConfig.parseSettings();
        }
    }

    static {
        // Common 配置
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        PAGE_MULTIPLIER = commonBuilder
                .defineInRange("pageMultiplier", 1, 1, 64);
        INDEPENDENT_UPLOADING_BUTTON = commonBuilder
                .define("independentUploadingButton", false);
        COMMON_SPEC = commonBuilder.build();

        // Client 配置
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        SHOW_ENCODER_PATTERN_PLAYER = clientBuilder
                .define("showEncoderPatternPlayer", true);
        PATTERN_TERMINAL_SHOW_SLOTS_DEFAULT = clientBuilder
                .define("patternTerminalShowSlotsDefault", true);
        OVERRIDE_AE2WT_PICKING = clientBuilder
                .define("overrideAE2WTPicking", false);
        CLIENT_SPEC = clientBuilder.build();

        // Server 配置
        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();
        serverBuilder.push("ae");
        PROVIDER_ROUND_ROBIN_ENABLE = serverBuilder
                .define("providerRoundRobinEnable", true);
        SMART_SCALING_MAX_MULTIPLIER = serverBuilder
                .defineInRange("smartScalingMaxMultiplier", 0, 0, 1048576);
        CRAFTING_PAUSE_THRESHOLD = serverBuilder
                .defineInRange("craftingPauseThreshold", 100000, 100, Integer.MAX_VALUE);
        serverBuilder.pop();

        serverBuilder.push("wireless");
        WIRELESS_MAX_RANGE = serverBuilder
                .defineInRange("wirelessMaxRange", 256.0D, 1.0D, 4096.0D);
        WIRELESS_CROSS_DIM_ENABLE = serverBuilder
                .define("wirelessCrossDimEnable", true);
        serverBuilder.pop();

        serverBuilder.push("ticker");
        TICKER_BASE_COST = serverBuilder
                .defineInRange("tickerBaseCost", 512, 0, Integer.MAX_VALUE);
        ALLOW_DISK_ENERGY = serverBuilder
                .define("allowDiskEnergy", true);
        TICKER_BLACKLIST = serverBuilder
                .defineListAllowEmpty(
                        List.of("tickerBlacklist"),
                        List::of,
                        String::new,
                        object -> object instanceof String value
                                && Pattern.matches("^#?\\w+:\\w+$", value)
                );
        TICKER_EXTERNAL_MULTIPLIER = serverBuilder
                .defineListAllowEmpty(
                        List.of("tickerExternalMultiplier"),
                        List::of,
                        String::new,
                        object -> object instanceof String value
                                && Pattern.matches("^#?\\w+:\\w+\\[[\\d.]+]$", value)
                );
        serverBuilder.pop();
        NEEDS_UPLOADING_CORE = serverBuilder
                .define("needsUploadingCore", true);
        SERVER_SPEC = serverBuilder.build();
    }
}
