package com.extendedae_plus

import com.extendedae_plus.common.init.InitObject
import com.extendedae_plus.common.registry.part.ticker.ParserTickerConfig
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec
import java.util.regex.Pattern

object EAEPConfig {
    // Spec

    private val specCommon: Triple<ModConfig.Type, ModConfigSpec, String>
    private val specClient: Triple<ModConfig.Type, ModConfigSpec, String>
    private val specServer: Triple<ModConfig.Type, ModConfigSpec, String>

    // Common

    lateinit var exProviderPageMultiplier: ModConfigSpec.IntValue
    lateinit var modDependencyTips: ModConfigSpec.BooleanValue

    // Client

    lateinit var independentUploadButton: ModConfigSpec.BooleanValue
    lateinit var showPatternEncoder: ModConfigSpec.BooleanValue
    lateinit var accessTerminalSlotsVisibleDefault: ModConfigSpec.BooleanValue
    lateinit var allowDiskEnergy: ModConfigSpec.BooleanValue
    lateinit var overrideAE2WTPicking: ModConfigSpec.BooleanValue

    // Server

    // - AE

    lateinit var providerRoundRobin: ModConfigSpec.BooleanValue
    lateinit var smartDoublingMaxMultiplier: ModConfigSpec.IntValue
    lateinit var smartDoublingAdapt: ModConfigSpec.BooleanValue // 没机会写完
    lateinit var craftingPauseThreshold: ModConfigSpec.IntValue

    // - Ticker

    lateinit var baseTickerEnergyCost: ModConfigSpec.IntValue
    lateinit var tickerBlacklist: ModConfigSpec.ConfigValue<MutableList<out String>>
    lateinit var tickerExternalMultiplier: ModConfigSpec.ConfigValue<MutableList<out String>>

    // - Assembler Matrix

    lateinit var baseCoreCrafterThreads: ModConfigSpec.IntValue
    lateinit var coreCrafterThreadAmplification: ModConfigSpec.IntValue
    lateinit var maximumCoreCrafterThreads: ModConfigSpec.IntValue
    lateinit var corePatternSlotMultiplier: ModConfigSpec.IntValue
    lateinit var needsUploadingPort: ModConfigSpec.BooleanValue

    init {
        this.specCommon = this.spec("common", ModConfig.Type.COMMON) { spec ->
            this.exProviderPageMultiplier = spec
                .defineInRange("pageMultiplier", 1, 1, 64)
            this.modDependencyTips = spec
                .define("dependencyTips", true)
        }

        this.specClient = this.spec("client", ModConfig.Type.CLIENT) { spec ->
            this.independentUploadButton = spec
                .define("independentUploadingButton", false)
            this.showPatternEncoder = spec
                .define("showEncoderPatternPlayer", true)
            this.accessTerminalSlotsVisibleDefault = spec
                .define("patternTerminalShowSlotsDefault", true)
            this.overrideAE2WTPicking = spec
                .define("overrideAE2WTPicking", false)
        }

        this.specServer = this.spec("server", ModConfig.Type.SERVER) { spec ->
            spec.section("ae") { section ->
                this.providerRoundRobin = section
                    .define("providerRoundRobinEnable", true)
                this.smartDoublingMaxMultiplier = section
                    .defineInRange("smartScalingMaxMultiplier", 0, 0, 1048576)
                this.smartDoublingAdapt = section
                    .define("smartDoublingAdapt", false)
                this.craftingPauseThreshold = section
                    .defineInRange("craftingPauseThreshold", 100, 100, Integer.MAX_VALUE)
            }

            spec.section("ticker") { section ->
                this.baseTickerEnergyCost = section
                    .defineInRange("tickerBaseCost", 512, 0, Integer.MAX_VALUE)
                this.allowDiskEnergy = section
                    .define("allowDiskEnergy", true)
                this.tickerBlacklist = section.defineListAllowEmpty(
                    listOf("tickerBlacklist"),
                    ::ArrayList,
                    ::String
                ) { it is CharSequence && Pattern.matches("^#?\\w+:\\w+$", it) }
                this.tickerExternalMultiplier = section.defineListAllowEmpty(
                    listOf("tickerExternalMultiplier"),
                    ::ArrayList,
                    ::String
                ) { it is CharSequence && Pattern.matches("^#?\\w+:\\w+\\[[\\d.]+]$", it) }
            }

            spec.section("assembler_matrix") { section ->
                this.baseCoreCrafterThreads = section
                    .worldRestart()
                    .defineInRange("baseCoreCrafterThreads", 32, 1, 64)
                this.coreCrafterThreadAmplification = section
                    .worldRestart()
                    .defineInRange("coreCrafterThreadAmplification", 32, 0, 64)
                this.maximumCoreCrafterThreads = section
                    .worldRestart()
                    .defineInRange("maximumCoreCrafterThreads", 128, 1, 256)
                this.corePatternSlotMultiplier = section
                    .worldRestart()
                    .defineInRange("corePatternSlotMultiplier", 4, 1, 16)
                this.needsUploadingPort = section
                    .define("needsUploadingPort", true)
            }
        }
    }

    @InitObject
    private fun init(eventBus: IEventBus, containerMod: ModContainer) {
        val register: Triple<ModConfig.Type, ModConfigSpec, String>.() -> Unit = {
            containerMod.registerConfig(
                this.first,
                this.second,
                "extendedae_plus/${this.third}.toml"
            )
        }

        register(this.specCommon)
        register(this.specClient)
        register(this.specServer)

        eventBus.addListener<ModConfigEvent.Loading> { this.reload(it.config) }
        eventBus.addListener<ModConfigEvent.Reloading> { this.reload(it.config) }
    }

    private fun reload(config: ModConfig) = when (config.spec) {
        this.specCommon.second -> {

        }

        this.specClient.second -> {

        }

        this.specServer.second -> {
            ParserTickerConfig.parseSettings()
        }

        else -> Unit
    }

    private inline fun spec(
        spec: String, type: ModConfig.Type, modifier: (ModConfigSpec.Builder) -> Unit
    ): Triple<ModConfig.Type, ModConfigSpec, String> {
        val builder = ModConfigSpec.Builder()
        modifier(builder)
        return Triple(type, builder.build(), spec)
    }

    private inline fun ModConfigSpec.Builder.section(
        section: String, modifier: (ModConfigSpec.Builder) -> Unit
    ): ModConfigSpec.Builder {
        this.push(section)
        modifier(this)
        this.pop()
        return this
    }
}
