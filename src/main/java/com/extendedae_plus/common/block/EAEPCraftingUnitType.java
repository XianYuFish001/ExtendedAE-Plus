package com.extendedae_plus.common.block;

import appeng.block.crafting.CraftingUnitBlock;
import appeng.block.crafting.ICraftingUnitType;
import com.extendedae_plus.common.init.ModBlocks;
import com.extendedae_plus.common.init.ModItems;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public enum EAEPCraftingUnitType implements ICraftingUnitType, StringRepresentable {
    ACCELERATOR_4x(0, 4),
    ACCELERATOR_16x(0, 16),
    ACCELERATOR_64x(0, 64),
    ACCELERATOR_256x(0, 256),
    ACCELERATOR_1024x(0, 1024);

    private final long storage;
    private final int threads;

    private static final List<DeferredBlock<CraftingUnitBlock>> UNIT_BLOCKS = new ArrayList<>();
    private static final List<DeferredItem<BlockItem>> UNIT_ITEMS = new ArrayList<>();

    public static void init() {
        for (var type : values()) {
            registerUnitType(type);
        }
    }

    private static void registerUnitType(EAEPCraftingUnitType type) {
        var holderBlock = ModBlocks.BLOCK.register(type.getSerializedName(), () -> new CraftingUnitBlock(type));
        var holderItem = ModItems.regCommonBlockItem(type.getSerializedName(), holderBlock);
        UNIT_BLOCKS.add(holderBlock);
        UNIT_ITEMS.add(holderItem);
    }

    EAEPCraftingUnitType(long storage, int threads) {
        this.storage = storage;
        this.threads = threads;
    }

    @Override
    public long getStorageBytes() {
        return this.storage;
    }

    @Override
    public int getAcceleratorThreads() {
        // 返回定义的真实线程数。AE2 原版在 CraftingCPUCluster.addBlockEntity 中对单块线程数
        // 有 16 的硬限制，但本模组已通过 Mixin 取消该限制，因此这里不再进行夹取。
        return this.threads;
    }

    @Override
    public Item getItemFromType() {
        return UNIT_ITEMS.get(this.ordinal()).get();
    }

    public DeferredBlock<CraftingUnitBlock> getBlock() {
        return UNIT_BLOCKS.get(this.ordinal());
    }

    @Override
    public String getSerializedName() {
        return this.toString().toLowerCase();
    }
}
