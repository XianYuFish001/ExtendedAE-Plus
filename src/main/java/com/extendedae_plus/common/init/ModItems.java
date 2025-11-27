package com.extendedae_plus.common.init;

import appeng.items.materials.UpgradeCardItem;
import appeng.items.parts.PartItem;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.item.*;
import com.extendedae_plus.common.item.infinityBigIntegerCell.InfinityBigIntegerCellItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class ModItems {
    public static final DeferredRegister.Items ITEM =
            DeferredRegister.createItems(ExtendedAEPlus.MODID);

    /// CraftingUnit Items are now in {@link EAEPCraftingUnitType#UNIT_ITEMS}
    public static final List<DeferredItem<?>> ITEMS = new ArrayList<>();

    public static final DeferredItem<BlockItem> WIRELESS_TRANSCEIVER =
            regCommonBlockItem("wireless_transceiver", ModBlocks.WIRELESS_TRANSCEIVER);
    public static final DeferredItem<BlockItem> ASSEMBLER_MATRIX_UPLOAD_CORE =
            regCommonBlockItem("assembler_matrix_upload_core", ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE);

    public static final DeferredItem<PartItem<?>> ENTITY_TICKER_PART_ITEM =
            regItem("entity_speed_ticker", EntitySpeedTickerPartItem::new);

    public static final DeferredItem<Item> INFINITY_BIGINTEGER_CELL_ITEM =
            regItem("infinity_biginteger_cell", InfinityBigIntegerCellItem::new);
    public static final DeferredItem<Item> PROVIDER_CONTROLLER =
            regItem("provider_controller", ItemProviderController::new);

    public static final DeferredItem<UpgradeCardItem> CHANNEL_CARD =
            regItem("channel_card", ChannelCardItem::new);
    public static final DeferredItem<UpgradeCardItem> CARD_AUTO_COMPLETION =
            regItem("card_auto_completion", ItemCardAutoCompletion::new);

    /// 随机数, 嘻嘻😋
    public static final DeferredItem<UpgradeCardItem> ENTITY_SPEED_CARD =
            ITEM.register("entity_speed_card", () -> new EntitySpeedCardItem(RandomGenerator.getDefault().nextInt(64)));

    public static <T extends Item> DeferredItem<T> regItem(String name, Supplier<T> factory) {
        var holder = ITEM.register(name, factory);
        ITEMS.add(holder);
        return holder;
    }
    
    public static DeferredItem<BlockItem> regCommonBlockItem(String name, DeferredBlock<?> block) {
        var holder = ITEM.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        ITEMS.add(holder);
        return holder;
    }
}
