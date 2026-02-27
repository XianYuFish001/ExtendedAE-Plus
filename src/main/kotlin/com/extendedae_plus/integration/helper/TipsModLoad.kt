package com.extendedae_plus.integration.helper

import com.extendedae_plus.EAEPConfig
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.util.UtilKeyBuilder
import com.extendedae_plus.util.UtilTextComponent
import com.fish.fishlib.util.keyBuilder.BuilderAdder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

enum class TipsModLoad(builderInfo: (BuilderInfo) -> Unit) {
    ExpandedAE({ info ->
        info
            .client {
                it.addStr("expandedae").buildInto()
            }
            .server(
                """
                    Using this mod with ExpandedAE may cause the following and more functions to become unavailable:
                      - Smart Doubling/Blocking
                      - Over-16-thread Accelerator
                      - Pattern Modify
                    (Toggle Config `DependencyTips` off to disable this tip)
                    """
            )
    });

    private val tipClient: (Player) -> Unit
    private val tipServer: (Logger) -> Unit

    init {
        val info = BuilderInfo()
        builderInfo(info)
        this.tipClient = info.tipClient
        this.tipServer = info.tipServer
    }

    private class BuilderInfo {
        var tipClient = { _: Player -> }
        var tipServer = { _: Logger -> }

        fun clientOriginal(tipClient: (Player) -> Unit): BuilderInfo {
            this.tipClient = tipClient
            return this
        }

        fun client(tipClient: (BuilderAdder<*>) -> Unit): BuilderInfo {
            val value = Component.empty()
            val builder = UtilKeyBuilder.of(Patterns.Message)
                .addStr("tips_mod_load")
                .bindAdder(value::append)
            tipClient(builder)
            builder.buildInto("confirm") { button ->
                button.withStyle { style ->
                    style
                        .withClickEvent(
                            UtilTextComponent.ClickEventCustomizable(
                                { EAEPConfig.ModDependencyTips = false },
                                UtilKeyBuilder.of(Patterns.Message)
                                    .addStr("tips_mod_load")
                                    .addStr("confirm")
                                    .addStr("callback")
                                    .build()
                            )
                        )
                        .withHoverEvent(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                UtilKeyBuilder.of(Patterns.Message)
                                    .addStr("tips_mod_load")
                                    .addStr("confirm")
                                    .addStr("hover")
                                    .build()
                            )
                        )
                }
            }
            return this.clientOriginal { it.displayClientMessage(value, false) }
        }

        fun server(tipServer: (Logger) -> Unit): BuilderInfo {
            this.tipServer = tipServer
            return this
        }

        fun server(tipServer: String) = this.server { it.warn(tipServer) }
    }

    @EventBusSubscriber(modid = ExtendedAEPlus.MODID)
    companion object {
        @SubscribeEvent
        private fun tipClient(event: ClientPlayerNetworkEvent.LoggingIn) {
            if (!EAEPConfig.ModDependencyTips) return
            if (FMLEnvironment.dist.isDedicatedServer) return
            entries
                .filter { ContextModLoaded.valueOf(it.name).shouldTip() }
                .forEach { it.tipClient(Minecraft.getInstance().player!!) }
        }

        @SubscribeEvent
        private fun tipServer(event: FMLDedicatedServerSetupEvent) {
            if (!EAEPConfig.ModDependencyTips) return
            val logger = LoggerFactory.getLogger("EAEP/DependencyTip")
            entries
                .filter { ContextModLoaded.valueOf(it.name).shouldTip() }
                .forEach { it.tipServer(logger) }
        }
    }
}