package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.dataComponent.DataSpeedCard;
import com.extendedae_plus.util.UtilGetKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.stream.Stream;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExtendedAEPlus.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(new UtilGetKey(UtilGetKey.creativeTab).addStr("main").build())
                    .icon(ModItems.WIRELESS_TRANSCEIVER::toStack)
                    .displayItems((params, output) -> {
                        ModItems.ITEMS.forEach(output::accept);
                        Stream.of(2, 4, 8, 16).map(DataSpeedCard::toStack).forEach(output::accept);
                    }).build());
}
