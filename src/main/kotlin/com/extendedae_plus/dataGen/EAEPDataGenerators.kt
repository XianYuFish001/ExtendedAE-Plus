package com.extendedae_plus.dataGen

import com.extendedae_plus.ExtendedAEPlus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
object EAEPDataGenerators {
    @SubscribeEvent
    fun register(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val providerLookup = event.lookupProvider
        val helperFile = event.existingFileHelper

        generator.addProvider(event.includeClient(), LangEN(output))
        generator.addProvider(event.includeClient(), LangZH(output))
        generator.addProvider(event.includeServer(), Recipe(output, providerLookup))

        val providerBlock = Tag.Block(output, providerLookup, helperFile)
        val providerItem = Tag.Item(output, providerLookup, helperFile, providerBlock)
        generator.addProvider(event.includeServer(), providerBlock)
        generator.addProvider(event.includeServer(), providerItem)

//        generator.addProvider(event.includeServer(), event.datapack())
    }
}
