package com.extendedae_plus.dataGen;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class Tag {
    public static class Item extends ItemTagsProvider {
        public Item(PackOutput output,
                    CompletableFuture<HolderLookup.Provider> providerLookup,
                    ExistingFileHelper helperFile, BlockTagsProvider providerBlock) {
            super(output, providerLookup, providerBlock.contentsGetter(), ExtendedAEPlus.MODID, helperFile);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {

        }
    }

    public static class Block extends BlockTagsProvider {
        public Block(PackOutput output,
                     CompletableFuture<HolderLookup.Provider> providerLookup,
                     @Nullable ExistingFileHelper existingFileHelper) {
            super(output, providerLookup, ExtendedAEPlus.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(ModBlocks.Blocks.stream()
                            .map(DeferredBlock::get)
                            .toArray(net.minecraft.world.level.block.Block[]::new));
        }
    }
}
