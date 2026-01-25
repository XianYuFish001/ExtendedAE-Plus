package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard;
import com.extendedae_plus.util.UtilTextComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.stream.Stream;

public final class ModCreativeTabs {
    @InitObject
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExtendedAEPlus.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(ModItems.WIRELESS_TRANSCEIVER::toStack)
                    .displayItems((params, output) -> {
                        ModItems.ITEMS.forEach(output::accept);
                        Stream.of(
                                new DataTickingCard(2, 16),
                                new DataTickingCard(4, 192),
                                new DataTickingCard(8, 512),
                                new DataTickingCard(16, 1024)
                        ).map(DataTickingCard::toStack).forEach(output::accept);
                    }).withTabFactory(builder -> new CreativeModeTab(builder) {
                        @Override
                        public Component getDisplayName() {
                            return UtilTextComponent.modNameColorful;
                        }
                    }).build());
}
