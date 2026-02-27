package com.extendedae_plus.common.init

import appeng.api.AECapabilities
import appeng.api.networking.IInWorldGridNodeHost
import appeng.block.AEBaseEntityBlock
import appeng.blockentity.AEBaseBlockEntity
import appeng.blockentity.crafting.CraftingBlockEntity
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.block.EAEPCraftingUnit
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter.TileAdvancedCrafter
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern.TileAdvancedPattern
import com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedSpeed.TileAdvancedSpeed
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.TileUpload
import com.extendedae_plus.common.registry.block.wirelessTransceiver.TileWirelessTransceiver
import com.fish.fishlib.common.InitObject
import com.fish.fishlib.util.extension.cast
import com.fish.fishlib.util.extension.invoke
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier
import net.neoforged.neoforge.capabilities.BlockCapability
import net.neoforged.neoforge.capabilities.ICapabilityProvider
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.commons.lang3.ArrayUtils

object EAEPTiles {
    @InitObject
    val Register: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE, ExtendedAEPlus.MODID
    )

    val Tiles = ArrayList<InfoTile<out BlockEntity>>()

    private val Capabilities: List<InfoCapability<in BlockEntity, *, *>> = listOf(
        InfoCapability(
            IInWorldGridNodeHost::class.java,
            AECapabilities.IN_WORLD_GRID_NODE_HOST
        )
    )

    @JvmField
    val WirelessTransceiver: DeferredHolder<BlockEntityType<*>, BlockEntityType<TileWirelessTransceiver>> =
        this.regCommonTile(
            TileWirelessTransceiver::class.java,
            ::TileWirelessTransceiver,
            EAEPBlocks.WirelessTransceiver
        )

    @JvmField
    val PortUpload: DeferredHolder<BlockEntityType<*>, BlockEntityType<TileUpload>> =
        this.regCommonTile(
            TileUpload::class.java,
            ::TileUpload,
            EAEPBlocks.PortUpload
        )

    @JvmField
    val CoreAdvancedCrafter: DeferredHolder<BlockEntityType<*>, BlockEntityType<TileAdvancedCrafter>> =
        this.regCommonTile(
            TileAdvancedCrafter::class.java,
            ::TileAdvancedCrafter,
            EAEPBlocks.CoreAdvancedCrafter
        )

    @JvmField
    val CoreAdvancedPattern: DeferredHolder<BlockEntityType<*>, BlockEntityType<TileAdvancedPattern>> =
        this.regCommonTile(
            TileAdvancedPattern::class.java,
            ::TileAdvancedPattern,
            EAEPBlocks.CoreAdvancedPattern
        )

    @JvmField
    val CoreAdvancedSpeed: DeferredHolder<BlockEntityType<*>, BlockEntityType<TileAdvancedSpeed>> =
        this.regCommonTile(
            TileAdvancedSpeed::class.java,
            ::TileAdvancedSpeed,
            EAEPBlocks.CoreAdvancedSpeed
        )

    // 提供一个 CraftingBlockEntity 的类型，允许附着在本模组自定义加速器方块上，绕过 AE2 默认类型的“有效方块列表”校验
    val UnitCraftingUniversal: DeferredHolder<BlockEntityType<*>, BlockEntityType<CraftingBlockEntity>> =
        Register.register("unit_crafting_universal") { ->
            val referenceType = Array<BlockEntityType<*>?>(1) {null}
            val type = BlockEntityType.Builder.of({ pos, state ->
                    CraftingBlockEntity(referenceType[0], pos, state)
                }, *EAEPCraftingUnit.entries
                    .map { it.block() }
                    .toTypedArray()
            ).build(null)
            referenceType[0] = type
            type
        }

    // Register
    fun <T : BlockEntity> regCommonTile(
        clazzBlockEntity: Class<T>,
        factory: BlockEntitySupplier<T>,
        firstBlock: DeferredBlock<*>,
        vararg otherBlocks: DeferredBlock<*>
    ): DeferredHolder<BlockEntityType<*>, BlockEntityType<T>> {
        val currentBlocks = if (otherBlocks.isNotEmpty()) {
            ArrayUtils.add(otherBlocks, firstBlock)
        } else arrayOf(firstBlock)

        val holder = Register.register(
            firstBlock.id.path
        ) { ->
            BlockEntityType.Builder.of(
                factory,
                *currentBlocks
                    .map(DeferredBlock<*>::get)
                    .toTypedArray()
            ).build(null)
        }
        Tiles += InfoTile(clazzBlockEntity, holder, *currentBlocks)
        return holder
    }

    fun regTileCapability(event: RegisterCapabilitiesEvent) = Capabilities.forEach { infoCapability ->
        Tiles.forEach { infoCapability.register(event, it) }
    }

    fun bindTileAE() = Tiles.forEach { info ->
        if (!AEBaseBlockEntity::class.java.isAssignableFrom(info.clazz)) return@forEach
        val clazz = info.clazz
        val type = info.holder()

        info.blocks.forEach {
            (it() as? AEBaseEntityBlock<*>)?.setBlockEntity(
                clazz.cast(),
                type.cast(),
                null, null
            )
        }
        AEBaseBlockEntity.registerBlockEntityItem(type, info.blocks[0].asItem())
    }


    class InfoTile<T : BlockEntity>(
        val clazz: Class<T>,
        val holder: DeferredHolder<BlockEntityType<*>, BlockEntityType<T>>,
        vararg val blocks: DeferredBlock<*>
    )

    @JvmRecord
    data class InfoCapability<THost : BlockEntity, TCapability, TContext>(
        val clazzCapability: Class<TCapability>,
        val capability: BlockCapability<TCapability, TContext>,
        val provider: ICapabilityProvider<THost, TContext, TCapability>? = null
    ) {
        fun register(event: RegisterCapabilitiesEvent, info: InfoTile<out THost>) {
            if (!this.clazzCapability.isAssignableFrom(info.clazz)) return

            val provider = this.provider ?: ICapabilityProvider { tile, _ -> this.clazzCapability.cast(tile) }
            event.registerBlockEntity(this.capability, info.holder.get(), provider)
        }
    }
}
