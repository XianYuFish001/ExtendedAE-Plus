package com.extendedae_plus

import com.extendedae_plus.common.init.InitObject
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(ExtendedAEPlus.MODID)
class ExtendedAEPlus(eventBus: IEventBus, containerMod: ModContainer) {
    init {
        InitObject(eventBus, containerMod)
    }

    companion object {
        const val MODID = "extendedae_plus"
        const val MODNAME = "ExtendedAE Plus"

        @JvmStatic
        fun getLocation(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, path)
    }
}