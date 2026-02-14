package com.extendedae_plus.dataGen;

import com.extendedae_plus.ExtendedAEPlus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class ModDataGenerators {
    @SubscribeEvent
    public static void register(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var providerLookup = event.getLookupProvider();
        var helperFile = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new LangEN(output));
        generator.addProvider(event.includeClient(), new LangZH(output));
        generator.addProvider(event.includeServer(), new Recipe(output, providerLookup));

        var providerBlock = new Tag.Block(output, providerLookup, helperFile);
        var providerItem = new Tag.Item(output, providerLookup, helperFile, providerBlock);
        generator.addProvider(event.includeServer(), providerBlock);
        generator.addProvider(event.includeServer(), providerItem);
    }
}
