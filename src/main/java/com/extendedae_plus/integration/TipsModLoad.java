package com.extendedae_plus.integration;

import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.extendedae_plus.util.UtilTextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public enum TipsModLoad {
    expandedAE(builder -> {
        UtilKeyBuilder.of(UtilKeyBuilder.message)
                .addStr("tips_mod_load")
                .bindAdder(builder::append)
                .addStr("expandedae")
                .buildInto()
                .buildInto("confirm", button -> button.withStyle(style -> style
                        .withClickEvent(new UtilTextComponent.ClickEventCustomizable(
                                () -> EAEPConfig.modDependencyTips.set(false),
                                UtilKeyBuilder.of(UtilKeyBuilder.message)
                                        .addStr("tips_mod_load")
                                        .addStr("confirm")
                                        .addStr("callback")
                                        .build()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                UtilKeyBuilder.of(UtilKeyBuilder.message)
                                        .addStr("tips_mod_load")
                                        .addStr("confirm")
                                        .addStr("hover")
                                        .build()))));
        return builder;
    }, """
            Using this mod with ExpandedAE may cause the following and more functions to become unavailable:
              - Smart Doubling/Blocking
              - Over-16-thread Accelerator
              - Pattern Modify
            (Toggle Config `DependencyTips` off to disable this tip)""")

    ;

    @SubscribeEvent
    private static void tipClient(ClientPlayerNetworkEvent.LoggingIn event) {
        if (EAEPConfig.modDependencyTips.isFalse()) return;
        if (FMLEnvironment.dist.isDedicatedServer()) return;
        Arrays.stream(TipsModLoad.values())
                .filter(entry -> ContextModLoaded.valueOf(entry.name()).shouldTip())
                .forEach(entry -> entry.tipClient.accept(event.getPlayer()));
    }

    @SubscribeEvent
    private static void tipServer(FMLDedicatedServerSetupEvent event) {
        if (EAEPConfig.modDependencyTips.isFalse()) return;
        var logger = LoggerFactory.getLogger("[EAEP/DependencyTip]");
        Arrays.stream(TipsModLoad.values())
                .filter(entry -> ContextModLoaded.valueOf(entry.name()).shouldTip())
                .forEach(entry -> entry.tipServer.accept(logger));
    }

    private Consumer<Player> tipClient;
    private Consumer<Logger> tipServer;

    TipsModLoad(Consumer<Player> tipClient, Consumer<Logger> tipServer) {
        this.tipClient = tipClient;
        this.tipServer = tipServer;
    }

    TipsModLoad(@Nullable UnaryOperator<MutableComponent> tipClient, String tipServer) {
        this(player -> {
            if (tipClient != null) player.displayClientMessage(tipClient.apply(Component.empty()), false);
        }, logger -> {
            if (!tipServer.isEmpty()) logger.warn(tipServer);
        });
    }

    TipsModLoad(UnaryOperator<MutableComponent> tipClient) {
        this(tipClient, "");
    }

    TipsModLoad(String tipServer) {
        this(null, tipServer);
    }
}