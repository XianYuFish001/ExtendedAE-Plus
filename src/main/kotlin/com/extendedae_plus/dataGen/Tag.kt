package com.extendedae_plus.dataGen

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.init.EAEPBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.ItemTagsProvider
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import net.neoforged.neoforge.registries.DeferredBlock
import java.util.concurrent.CompletableFuture

class Tag {
    class Item(
        output: PackOutput,
        providerLookup: CompletableFuture<HolderLookup.Provider>,
        helperFile: ExistingFileHelper,
        providerBlock: BlockTagsProvider
    ) : ItemTagsProvider(
        output,
        providerLookup,
        providerBlock.contentsGetter(),
        ExtendedAEPlus.MODID,
        helperFile
    ) {
        override fun addTags(provider: HolderLookup.Provider) {
        }
    }

    class Block(
        output: PackOutput,
        providerLookup: CompletableFuture<HolderLookup.Provider>,
        existingFileHelper: ExistingFileHelper
    ) : BlockTagsProvider(
        output,
        providerLookup,
        ExtendedAEPlus.MODID,
        existingFileHelper
    ) {
        override fun addTags(provider: HolderLookup.Provider) {
            val blocks = EAEPBlocks.Blocks
                .map(DeferredBlock<*>::get)
                .toTypedArray()

            this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(*blocks)
            this.tag(BlockTags.NEEDS_STONE_TOOL).add(*blocks)
        }
    }
}
