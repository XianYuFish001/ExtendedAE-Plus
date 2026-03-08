package com.extendedae_plus

import com.extendedae_plus.common.registry.part.ticker.ParserTickerConfig
import com.fish.fishlib.common.InitObject
import com.fish.fishlib.config.HelperConfig
import com.fish.fishlib.config.HelperConfig.Companion.bind
import com.fish.fishlib.config.section
import com.fish.fishlib.config.spec
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent
import java.util.regex.Pattern

object EAEPConfig {
    // Helper

    private val HelperCommon: HelperConfig = HelperConfig(this::SpecCommon)
    private val HelperClient: HelperConfig = HelperConfig(this::SpecClient)
    private val HelperServer: HelperConfig = HelperConfig(this::SpecServer)

    // Common

    var EXProviderPageMultiplier: Int by HelperCommon
    var ModDependencyTips: Boolean by HelperCommon

    // Client

    var IndependentUploadButton: Boolean by HelperClient
    var ShowPatternEncoder: Boolean by HelperClient
    var AccessTerminalSlotsVisibleDefault: Boolean by HelperClient
    var OverrideAE2WTPicking: Boolean by HelperClient

    // Server

    // - AE

    var ProviderRoundRobin: Boolean by HelperServer
    var SmartDoublingMaxMultiplier: Int by HelperServer
    var SmartDoublingAdapt: Boolean by HelperServer // 没机会写完
    var CraftingPauseThreshold: Int by HelperServer

    // - Ticker

    var BaseTickerEnergyCost: Int by HelperServer
    var AllowDiskEnergy: Boolean by HelperServer
    var TickerBlacklist: MutableList<out String> by HelperServer
    var TickerExternalMultiplier: MutableList<out String> by HelperServer

    // - Assembler Matrix

    var BaseCoreCrafterThreads: Int by HelperServer
    var CoreCrafterThreadAmplification: Int by HelperServer
    var MaximumCoreCrafterThreads: Int by HelperServer
    var CorePatternSlotMultiplier: Int by HelperServer
    var NeedsUploadingPort: Boolean by HelperServer

    // Spec

    private val SpecCommon by spec(ModConfig.Type.COMMON) { spec ->
        spec.defineInRange("pageMultiplier", 1, 1, 64)
            .bind(HelperCommon, ::EXProviderPageMultiplier)
        spec.define("dependencyTips", true)
            .bind(HelperCommon, ::ModDependencyTips)
    }

    private val SpecClient by spec(ModConfig.Type.CLIENT) { spec ->
        spec.define("independentUploadingButton", false)
            .bind(HelperClient, ::IndependentUploadButton)
        spec.define("showEncoderPatternPlayer", true)
            .bind(HelperClient, ::ShowPatternEncoder)
        spec.define("patternTerminalShowSlotsDefault", true)
            .bind(HelperClient, ::AccessTerminalSlotsVisibleDefault)
        spec.define("overrideAE2WTPicking", false)
            .bind(HelperClient, ::OverrideAE2WTPicking)
    }

    private val SpecServer by spec(ModConfig.Type.SERVER) { spec ->
        spec.section("ae") { section ->
            section.define("providerRoundRobinEnable", true)
                .bind(HelperServer, ::ProviderRoundRobin)
            section.defineInRange("smartScalingMaxMultiplier", 0, 0, 1048576)
                .bind(HelperServer, ::SmartDoublingMaxMultiplier)
            section.define("smartDoublingAdapt", false)
                .bind(HelperServer, ::SmartDoublingAdapt)
            section.defineInRange("craftingPauseThreshold", 100, 100, Integer.MAX_VALUE)
                .bind(HelperServer, ::CraftingPauseThreshold)
        }

        spec.section("ticker") { section ->
            section.defineInRange("tickerBaseCost", 512, 0, Integer.MAX_VALUE)
                .bind(HelperServer, ::BaseTickerEnergyCost)
            section.define("allowDiskEnergy", true)
                .bind(HelperServer, ::AllowDiskEnergy)
            section.defineListAllowEmpty(
                listOf("tickerBlacklist"),
                ::ArrayList,
                ::String
            ) { it is CharSequence && Pattern.matches("^#?\\w+:\\w+$", it) }
                .bind(HelperServer, ::TickerBlacklist)
            section.defineListAllowEmpty(
                listOf("tickerExternalMultiplier"),
                ::ArrayList,
                ::String
            ) { it is CharSequence && Pattern.matches("^#?\\w+:\\w+\\[[\\d.]+]$", it) }
                .bind(HelperServer, ::TickerExternalMultiplier)
        }

        spec.section("assembler_matrix") { section ->
            section
                .worldRestart()
                .defineInRange("baseCoreCrafterThreads", 32, 1, 64)
                .bind(HelperServer, ::BaseCoreCrafterThreads)
            section
                .worldRestart()
                .defineInRange("coreCrafterThreadAmplification", 32, 0, 64)
                .bind(HelperServer, ::CoreCrafterThreadAmplification)
            section
                .worldRestart()
                .defineInRange("maximumCoreCrafterThreads", 128, 1, 256)
                .bind(HelperServer, ::MaximumCoreCrafterThreads)
            section
                .worldRestart()
                .defineInRange("corePatternSlotMultiplier", 4, 1, 16)
                .bind(HelperServer, ::CorePatternSlotMultiplier)
            section.define("needsUploadingPort", true)
                .bind(HelperServer, ::NeedsUploadingPort)
        }
    }

    @InitObject
    private fun init(eventBus: IEventBus, containerMod: ModContainer) {
        HelperCommon.init(containerMod)
        HelperClient.init(containerMod)
        HelperServer.init(containerMod)

        eventBus.addListener<ModConfigEvent.Loading> { this.reload(it.config) }
        eventBus.addListener<ModConfigEvent.Reloading> { this.reload(it.config) }
    }

    private fun reload(config: ModConfig) = when (config.spec) {
        SpecCommon -> {

        }

        SpecClient -> {

        }

        SpecServer -> {
            ParserTickerConfig.parseSettings()
        }

        else -> Unit
    }
}
