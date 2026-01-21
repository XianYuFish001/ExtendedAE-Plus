package com.extendedae_plus;

import com.extendedae_plus.common.init.*;
import com.extendedae_plus.integration.ContextModLoaded;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ExtendedAEPlus.MODID)
public class ExtendedAEPlus {
    public static final String MODID = "extendedae_plus";
    public static final String MODNAME = "ExtendedAE Plus";

    public ExtendedAEPlus(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCK.register(modEventBus);
        ModItems.ITEM.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPE.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModMenuTypes.MENU.register(modEventBus);
        ModDataComponents.COMPONENT.register(modEventBus);

        EAEPConfig.init(modContainer);
        ContextModLoaded.init();
    }

    public static ResourceLocation getLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

