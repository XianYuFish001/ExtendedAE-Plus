package com.extendedae_plus;

import com.extendedae_plus.common.init.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ExtendedAEPlus.MODID)
public class ExtendedAEPlus {
    public static final String MODID = "extendedae_plus";

    public ExtendedAEPlus(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, EAEPConfig.COMMON_SPEC, "extendedae_plus/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, EAEPConfig.CLIENT_SPEC, "extendedae_plus/client.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, EAEPConfig.SERVER_SPEC, "extendedae_plus/server.toml");
    }

    public static ResourceLocation getLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}

