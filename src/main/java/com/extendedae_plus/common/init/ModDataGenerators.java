package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.dataGen.LangEN;
import com.extendedae_plus.dataGen.LangZH;
import com.extendedae_plus.dataGen.Recipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class ModDataGenerators {
    @SubscribeEvent
    public static void register(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new LangEN(output));
        generator.addProvider(event.includeClient(), new LangZH(output));
        generator.addProvider(event.includeServer(), new Recipe(output, lookupProvider));
    }
}
