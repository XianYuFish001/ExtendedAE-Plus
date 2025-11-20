package com.extendedae_plus.common.init;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import com.glodblock.github.extendedae.common.EAESingletons;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.pedroksl.advanced_ae.common.definitions.AAEBlocks;
import net.pedroksl.advanced_ae.common.definitions.AAEItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModUpgradeCards {
    /// 在commonSetup时注册
    public static void init() {
        Upgrades.add(AEItems.ENERGY_CARD, ModItems.ENTITY_TICKER_PART_ITEM.get(), 8);
        Upgrades.add(ModItems.ENTITY_SPEED_CARD.get(), ModItems.ENTITY_TICKER_PART_ITEM.get(), 4);

        String interfaceGroup = GuiText.Interface.getTranslationKey();
        String patternProviderGroup = "group.pattern_provider.name";
        String ioBusGroup = GuiText.IOBuses.getTranslationKey();
        String storageGroup = "group.storage.name";

        var channelCardGroups = List.of(
                List.of(
                        AEBlocks.INTERFACE,
                        AEParts.INTERFACE,
                        EAESingletons.EX_INTERFACE,
                        EAESingletons.EX_INTERFACE_PART,
                        EAESingletons.OVERSIZE_INTERFACE,
                        EAESingletons.OVERSIZE_INTERFACE_PART
                ),
                new ArrayList<>(List.of(
                        AEBlocks.PATTERN_PROVIDER,
                        AEParts.PATTERN_PROVIDER,
                        EAESingletons.EX_PATTERN_PROVIDER,
                        EAESingletons.EX_PATTERN_PROVIDER_PART
                )),
                new ArrayList<>(List.of(
                        AEParts.IMPORT_BUS,
                        AEParts.EXPORT_BUS,
                        EAESingletons.EX_IMPORT_BUS,
                        EAESingletons.EX_EXPORT_BUS,
                        EAESingletons.TAG_EXPORT_BUS,
                        EAESingletons.MOD_EXPORT_BUS,
                        EAESingletons.PRECISE_EXPORT_BUS,
                        EAESingletons.THRESHOLD_EXPORT_BUS
                )),
                List.of(
                        AEParts.STORAGE_BUS,
                        EAESingletons.TAG_STORAGE_BUS,
                        EAESingletons.MOD_STORAGE_BUS,
                        EAESingletons.PRECISE_STORAGE_BUS
                )
        );
        if (ModList.get().isLoaded("advanced_ae")) {
            channelCardGroups.get(1).addAll(List.of(
                    AAEBlocks.SMALL_ADV_PATTERN_PROVIDER,
                    AAEBlocks.ADV_PATTERN_PROVIDER,
                    AAEItems.SMALL_ADV_PATTERN_PROVIDER,
                    AAEItems.ADV_PATTERN_PROVIDER
            ));

            channelCardGroups.get(2).add(AAEItems.IMPORT_EXPORT_BUS);
            channelCardGroups.get(2).add(AAEItems.STOCK_EXPORT_BUS);
        }

        register(ModItems.CHANNEL_CARD, channelCardGroups.get(0), 1, interfaceGroup);
        register(ModItems.CHANNEL_CARD, channelCardGroups.get(1), 1, patternProviderGroup);
        register(ModItems.CHANNEL_CARD, channelCardGroups.get(2), 1, ioBusGroup);
        register(ModItems.CHANNEL_CARD, channelCardGroups.get(3), 1, storageGroup);
    }

    private static void register(DeferredItem<?> card, Collection<ItemLike> machines, int maxSupported, String tooltipGroup) {
        machines.forEach(machine -> Upgrades.add(card.get(), machine, maxSupported, tooltipGroup));
    }
}