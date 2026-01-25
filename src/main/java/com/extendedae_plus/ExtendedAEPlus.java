package com.extendedae_plus;

import com.extendedae_plus.common.init.InitObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ExtendedAEPlus.MODID)
public class ExtendedAEPlus {
    public static final String MODID = "extendedae_plus";
    public static final String MODNAME = "ExtendedAE Plus";

    public ExtendedAEPlus(IEventBus modEventBus, ModContainer modContainer) {
        InitObject.Initializer.init(modEventBus, modContainer);
    }

    public static ResourceLocation getLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

