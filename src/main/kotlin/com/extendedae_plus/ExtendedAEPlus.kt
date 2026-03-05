package com.extendedae_plus

import com.extendedae_plus.integration.helper.ManagerIntegration
import com.fish.fishlib.common.InitializerObject
import com.fish.fishlib.network.InitializerPacket
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(ExtendedAEPlus.MODID)
class ExtendedAEPlus(eventBus: IEventBus, containerMod: ModContainer) {
    init {
        InitializerObject(eventBus, containerMod)
        InitializerPacket(containerMod)
        ManagerIntegration.init()
    }

    companion object {
        const val MODID = "extendedae_plus"
        const val MODNAME = "ExtendedAE Plus"

        @JvmStatic
        fun getLocation(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, path)
    }
}