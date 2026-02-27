package com.extendedae_plus.common.init

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard
import com.extendedae_plus.util.UtilTextComponent
import com.fish.fishlib.common.InitObject
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object EAEPCreativeTabs {
    @InitObject
    val Register: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExtendedAEPlus.MODID)

    val Main: DeferredHolder<CreativeModeTab, CreativeModeTab> = Register.register("main") { ->
        CreativeModeTab.builder()
            .icon(EAEPItems.WirelessTransceiver::toStack)
            .displayItems { _, output ->
                EAEPItems.Items.forEach(output::accept)

                listOf(
                    DataTickingCard(2, 16),
                    DataTickingCard(4, 192),
                    DataTickingCard(8, 512),
                    DataTickingCard(16, 1024)
                ).map(DataTickingCard::toStack).forEach(output::accept)
            }.withTabFactory {
                object : CreativeModeTab(it) {
                    override fun getDisplayName() = UtilTextComponent.ModNameColorful
                }
            }.build()
    }
}
