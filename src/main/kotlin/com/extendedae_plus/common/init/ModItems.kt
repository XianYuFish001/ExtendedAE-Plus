package com.extendedae_plus.common.init

import appeng.items.materials.UpgradeCardItem
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.item.ItemProviderController
import com.extendedae_plus.common.registry.item.ItemTicker
import com.extendedae_plus.common.registry.item.infinityBigIntegerCell.InfinityBigIntegerCellItem
import com.extendedae_plus.common.registry.item.priorityTool.ItemPriorityTool
import com.extendedae_plus.common.registry.item.upgradeCard.CardAutoCompletion
import com.extendedae_plus.common.registry.item.upgradeCard.CardChannel
import com.extendedae_plus.common.registry.item.upgradeCard.CardTicking
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier
import java.util.random.RandomGenerator

object ModItems {
    @InitObject
    val Register: DeferredRegister.Items = DeferredRegister.createItems(ExtendedAEPlus.MODID)

    /** CraftingUnit Items are now in [com.extendedae_plus.common.registry.block.EAEPCraftingUnitType.UNIT_ITEMS] */
    val Items = ArrayList<DeferredItem<*>>()

    @JvmField
    val WirelessTransceiver = this.regCommonBlockItem(ModBlocks.WirelessTransceiver)
    @JvmField
    val PortUpload = this.regCommonBlockItem(ModBlocks.PortUpload)
    @JvmField
    val CoreAdvancedCrafter = this.regCommonBlockItem(ModBlocks.CoreAdvancedCrafter)
    @JvmField
    val CoreAdvancedPattern = this.regCommonBlockItem(ModBlocks.CoreAdvancedPattern)
    @JvmField
    val CoreAdvancedSpeed = this.regCommonBlockItem(ModBlocks.CoreAdvancedSpeed)

    @JvmField
    val Ticker = this.regItem("ticker", ::ItemTicker)

    @JvmField
    val CellInfinity = this.regItem("infinity_biginteger_cell", ::InfinityBigIntegerCellItem)
    @JvmField
    val ControllerProvider = this.regItem("provider_controller", ::ItemProviderController)
    @JvmField
    val PriorityTool = this.regItem("priority_tool", ::ItemPriorityTool)

    @JvmField
    val CardChannel = this.regItem("channel_card", ::CardChannel)
    @JvmField
    val CardAutoCompletion = this.regItem("card_auto_completion", ::CardAutoCompletion)

    /** 随机数, 嘻嘻😋 */
    @JvmField
    val CardTicking: DeferredItem<UpgradeCardItem> = Register.register(
        "card_ticking",
        Supplier { CardTicking(RandomGenerator.getDefault().nextInt(64), 114514) }
    )

    fun <T : Item> regItem(name: String, constructor: () -> T): DeferredItem<T> {
        val holder = Register.register(name, constructor)
        Items.add(holder)
        return holder
    }

    fun regCommonBlockItem(block: DeferredBlock<*>): DeferredItem<BlockItem> =
        this.regCommonBlockItem(block.id.path, block)

    fun regCommonBlockItem(name: String, block: DeferredBlock<*>): DeferredItem<BlockItem> =
        this.regItem(name) { BlockItem(block.get(), Item.Properties()) }
}
