package com.extendedae_plus;

import com.extendedae_plus.common.init.InitObject;
import com.extendedae_plus.common.registry.part.ticker.ParserTickerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.regex.Pattern;

public class EAEPConfig {
    static final ModConfigSpec specCommon;
    public static final ModConfigSpec.IntValue exProviderPageMultiplier;
    public static final ModConfigSpec.BooleanValue modDependencyTips;

    static final ModConfigSpec specClient;
    public static final ModConfigSpec.BooleanValue independentUploadButton;
    public static final ModConfigSpec.BooleanValue showPatternEncoder;
    public static final ModConfigSpec.BooleanValue accessTerminalSlotsVisibleDefault;
    public static final ModConfigSpec.BooleanValue allowDiskEnergy;
    public static final ModConfigSpec.BooleanValue overrideAE2WTPicking;

    static final ModConfigSpec specServer;
    // AE
    public static final ModConfigSpec.BooleanValue providerRoundRobin;
    public static final ModConfigSpec.IntValue smartDoublingMaxMultiplier;
    public static final ModConfigSpec.BooleanValue smartDoublingAdapt;
    public static final ModConfigSpec.IntValue craftingPauseThreshold;
    // Ticker
    public static final ModConfigSpec.IntValue baseTickerEnergyCost;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> tickerBlacklist;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> tickerExternalMultiplier;
    // Assembler Matrix
    public static final ModConfigSpec.IntValue baseCoreCrafterThreads;
    public static final ModConfigSpec.IntValue coreCrafterThreadAmplification;
    public static final ModConfigSpec.IntValue maximumCoreCrafterThreads;
    public static final ModConfigSpec.IntValue corePatternSlotMultiplier;
    public static final ModConfigSpec.BooleanValue needsUploadingPort;

    @InitObject
    private static void init(IEventBus eventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, EAEPConfig.specCommon, "extendedae_plus/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, EAEPConfig.specClient, "extendedae_plus/client.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, EAEPConfig.specServer, "extendedae_plus/server.toml");

        eventBus.addListener(ModConfigEvent.Loading.class, event -> reloadSetting(event.getConfig()));
        eventBus.addListener(ModConfigEvent.Reloading.class, event -> reloadSetting(event.getConfig()));
    }

    private static void reloadSetting(ModConfig config) {
        if (config.getSpec() == specServer) {
            ParserTickerConfig.parseSettings();
        }
    }

    static {
        // Common
        var builderCommon = new ModConfigSpec.Builder();
        exProviderPageMultiplier = builderCommon
                .defineInRange("pageMultiplier", 1, 1, 64);
        modDependencyTips = builderCommon
                .define("dependencyTips", true);
        specCommon = builderCommon.build();

        // Client
        var builderClient = new ModConfigSpec.Builder();
        // TODO 记录到commit
        independentUploadButton = builderClient
                .define("independentUploadingButton", false);
        showPatternEncoder = builderClient
                .define("showEncoderPatternPlayer", true);
        accessTerminalSlotsVisibleDefault = builderClient
                .define("patternTerminalShowSlotsDefault", true);
        overrideAE2WTPicking = builderClient
                .define("overrideAE2WTPicking", false);
        specClient = builderClient.build();

        // Server
        var builderServer = new ModConfigSpec.Builder();
        builderServer.push("ae");
        providerRoundRobin = builderServer
                .define("providerRoundRobinEnable", true);
        smartDoublingMaxMultiplier = builderServer
                .defineInRange("smartScalingMaxMultiplier", 0, 0, 1048576);
        smartDoublingAdapt = builderServer
                .define("smartDoublingAdapt", false);
        craftingPauseThreshold = builderServer
                .defineInRange("craftingPauseThreshold", 100, 100, Integer.MAX_VALUE);
        builderServer.pop();

        builderServer.push("ticker");
        baseTickerEnergyCost = builderServer
                .defineInRange("tickerBaseCost", 512, 0, Integer.MAX_VALUE);
        allowDiskEnergy = builderServer
                .define("allowDiskEnergy", true);
        tickerBlacklist = builderServer
                .defineListAllowEmpty(
                        List.of("tickerBlacklist"),
                        List::of,
                        String::new,
                        object -> object instanceof CharSequence value
                                && Pattern.matches("^#?\\w+:\\w+$", value)
                );
        tickerExternalMultiplier = builderServer
                .defineListAllowEmpty(
                        List.of("tickerExternalMultiplier"),
                        List::of,
                        String::new,
                        object -> object instanceof CharSequence value
                                && Pattern.matches("^#?\\w+:\\w+\\[[\\d.]+]$", value)
                );
        builderServer.pop();

        builderServer.push("assembler_matrix");
        baseCoreCrafterThreads = builderServer
                .worldRestart()
                .defineInRange("baseCoreCrafterThreads", 32, 1, 64);
        coreCrafterThreadAmplification = builderServer
                .worldRestart()
                .defineInRange("coreCrafterThreadAmplification", 32, 0, 64);
        maximumCoreCrafterThreads = builderServer
                .worldRestart()
                .defineInRange("maximumCoreCrafterThreads", 128, 1, 256);
        corePatternSlotMultiplier = builderServer
                .worldRestart()
                .defineInRange("corePatternSlotMultiplier", 4, 1, 16);
        needsUploadingPort = builderServer
                .define("needsUploadingPort", true);
        builderServer.pop();

        specServer = builderServer.build();
    }
}
