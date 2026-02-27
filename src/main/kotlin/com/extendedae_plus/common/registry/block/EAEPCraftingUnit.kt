package com.extendedae_plus.common.registry.block

import appeng.block.crafting.CraftingUnitBlock
import appeng.block.crafting.ICraftingUnitType
import com.extendedae_plus.common.init.EAEPBlocks
import com.extendedae_plus.common.init.EAEPItems
import com.fish.fishlib.common.InitObject
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.BlockItem
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem

enum class EAEPCraftingUnit(
    private val storage: Long,
    private val threads: Int
) : ICraftingUnitType, StringRepresentable {
    ACCELERATOR_4x(0, 4),
    ACCELERATOR_16x(0, 16),
    ACCELERATOR_64x(0, 64),
    ACCELERATOR_256x(0, 256),
    ACCELERATOR_1024x(0, 1024);

    override fun getStorageBytes() = this.storage

    /**
     * @see com.extendedae_plus.mixin.core.ae2.MixinClusterCPU.unlimit
     */
    override fun getAcceleratorThreads() = this.threads

    override fun getItemFromType(): BlockItem = Items[this.ordinal].get()

    val block: DeferredBlock<CraftingUnitBlock>
        get() = Blocks[this.ordinal]

    override fun getSerializedName() = this.toString().lowercase()

    companion object {
        private val Blocks = ArrayList<DeferredBlock<CraftingUnitBlock>>()
        private val Items = ArrayList<DeferredItem<BlockItem>>()

        @InitObject(priority = 90)
        private fun init() {
            for (type in entries) {
                this.registerUnitType(type)
            }
        }

        private fun registerUnitType(type: EAEPCraftingUnit) {
            val holderBlock = EAEPBlocks.register(type.getSerializedName()) { CraftingUnitBlock(type) }
            val holderItem = EAEPItems.regCommonBlockItem(type.getSerializedName(), holderBlock)
            Blocks.add(holderBlock)
            Items.add(holderItem)
        }
    }
}
