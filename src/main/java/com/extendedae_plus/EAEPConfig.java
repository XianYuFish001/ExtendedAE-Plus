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
    // AE
    public static final ModConfigSpec.BooleanValue PROVIDER_ROUND_ROBIN_ENABLE;
    public static final ModConfigSpec.IntValue SMART_SCALING_MAX_MULTIPLIER;
    public static final ModConfigSpec.IntValue CRAFTING_PAUSE_THRESHOLD;
    // Wireless
    public static final ModConfigSpec.DoubleValue WIRELESS_MAX_RANGE;
    public static final ModConfigSpec.BooleanValue WIRELESS_CROSS_DIM_ENABLE;
    // Ticker
    public static final ModConfigSpec.IntValue TICKER_BASE_COST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TICKER_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TICKER_EXTERNAL_MULTIPLIER;
    // Assembler Matrix
    public static final ModConfigSpec.IntValue BASE_CORE_CRAFTER_THREADS;
    public static final ModConfigSpec.IntValue CORE_CRAFTER_THREAD_AMPLIFICATION;
    public static final ModConfigSpec.IntValue MAXIMUM_CORE_CRAFTER_THREADS;
    public static final ModConfigSpec.IntValue CORE_PATTERN_SLOT_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue NEEDS_UPLOADING_PORT;

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
        // Common
        var builderCommon = new ModConfigSpec.Builder();
        PAGE_MULTIPLIER = builderCommon
                .defineInRange("pageMultiplier", 1, 1, 64);
        INDEPENDENT_UPLOADING_BUTTON = builderCommon
                .define("independentUploadingButton", false);
        COMMON_SPEC = builderCommon.build();

        // Client
        var builderClient = new ModConfigSpec.Builder();
        SHOW_ENCODER_PATTERN_PLAYER = builderClient
                .define("showEncoderPatternPlayer", true);
        PATTERN_TERMINAL_SHOW_SLOTS_DEFAULT = builderClient
                .define("patternTerminalShowSlotsDefault", true);
        OVERRIDE_AE2WT_PICKING = builderClient
                .define("overrideAE2WTPicking", false);
        CLIENT_SPEC = builderClient.build();

        // Server
        var builderServer = new ModConfigSpec.Builder();
        builderServer.push("ae");
        PROVIDER_ROUND_ROBIN_ENABLE = builderServer
                .define("providerRoundRobinEnable", true);
        SMART_SCALING_MAX_MULTIPLIER = builderServer
                .defineInRange("smartScalingMaxMultiplier", 0, 0, 1048576);
        CRAFTING_PAUSE_THRESHOLD = builderServer
                .defineInRange("craftingPauseThreshold", 100000, 100, Integer.MAX_VALUE);
        builderServer.pop();

        builderServer.push("wireless");
        WIRELESS_MAX_RANGE = builderServer
                .defineInRange("wirelessMaxRange", 256.0D, 1.0D, 4096.0D);
        WIRELESS_CROSS_DIM_ENABLE = builderServer
                .define("wirelessCrossDimEnable", true);
        builderServer.pop();

        builderServer.push("ticker");
        TICKER_BASE_COST = builderServer
                .defineInRange("tickerBaseCost", 512, 0, Integer.MAX_VALUE);
        ALLOW_DISK_ENERGY = builderServer
                .define("allowDiskEnergy", true);
        TICKER_BLACKLIST = builderServer
                .defineListAllowEmpty(
                        List.of("tickerBlacklist"),
                        List::of,
                        String::new,
                        object -> object instanceof String value
                                && Pattern.matches("^#?\\w+:\\w+$", value)
                );
        TICKER_EXTERNAL_MULTIPLIER = builderServer
                .defineListAllowEmpty(
                        List.of("tickerExternalMultiplier"),
                        List::of,
                        String::new,
                        object -> object instanceof String value
                                && Pattern.matches("^#?\\w+:\\w+\\[[\\d.]+]$", value)
                );
        builderServer.pop();

        builderServer.push("assembler_matrix");
        BASE_CORE_CRAFTER_THREADS = builderServer
                .worldRestart()
                .defineInRange("baseCoreCrafterThreads", 32, 1, 64);
        CORE_CRAFTER_THREAD_AMPLIFICATION = builderServer
                .worldRestart()
                .defineInRange("coreCrafterThreadAmplification", 32, 0, 64);
        MAXIMUM_CORE_CRAFTER_THREADS = builderServer
                .worldRestart()
                .defineInRange("maximumCoreCrafterThreads", 128, 1, 256);
        CORE_PATTERN_SLOT_MULTIPLIER = builderServer
                .worldRestart()
                .defineInRange("corePatternSlotMultiplier", 4, 1, 16);
        NEEDS_UPLOADING_PORT = builderServer
                .define("needsUploadingPort", true);
        builderServer.pop();

        SERVER_SPEC = builderServer.build();
    }
}
