package com.extendedae_plus

import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(value = ExtendedAEPlus.MODID, dist = [Dist.CLIENT])
class ExtendedAEPlusClient(container: ModContainer) {
    init {
        container.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory(::ConfigurationScreen)
        )
    }
}
