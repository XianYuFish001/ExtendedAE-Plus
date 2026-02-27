package com.extendedae_plus.client.render.crafting

import appeng.client.render.crafting.AbstractCraftingUnitModelProvider
import appeng.client.render.crafting.LightBakedModel
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.registry.block.EAEPCraftingUnit
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.util.RandomSource
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.client.ChunkRenderTypeSet
import net.neoforged.neoforge.client.model.data.ModelData
import java.util.function.Function

/**
 * 形成态光照模型。
 */
class EAEPCraftingCubeModelProvider(type: EAEPCraftingUnit) : AbstractCraftingUnitModelProvider<EAEPCraftingUnit>(type) {
    override fun getMaterials() = Materials

    override fun getBakedModel(spriteGetter: Function<Material?, TextureAtlasSprite?>): BakedModel {
        val ringCorner = spriteGetter.apply(RING_CORNER)
        val ringSideHor = spriteGetter.apply(RING_SIDE_HOR)
        val ringSideVer = spriteGetter.apply(RING_SIDE_VER)

        return object : LightBakedModel(
            ringCorner, ringSideHor, ringSideVer,
            spriteGetter.apply(LIGHT_BASE), this.getLightMaterial(spriteGetter)
        ) {
            override fun getRenderTypes(state: BlockState, rand: RandomSource, data: ModelData) = Cutout
        }
    }

    private fun getLightMaterial(textureGetter: Function<Material?, TextureAtlasSprite?>) =
        textureGetter.apply(UNITS_LIGHT[this.type])

    companion object {
        val Cutout: ChunkRenderTypeSet = ChunkRenderTypeSet.of(RenderType.cutout())
        private val Materials = ArrayList<Material>()

        //将环形边框与基础发光底图放在本模组命名空间
        private val RING_CORNER = texture("ring_corner")
        private val RING_SIDE_HOR = texture("ring_side_hor")
        private val RING_SIDE_VER = texture("ring_side_ver")
        private val LIGHT_BASE = texture("light_base")

        // 亮面贴图（formed 时使用）
        private val UNITS_LIGHT = HashMap<EAEPCraftingUnit, Material>()

        init {
            for (unit in EAEPCraftingUnit.entries) {
                UNITS_LIGHT[unit] = texture(unit.serializedName + "_light")
            }
        }

        private fun texture(name: String): Material {
            val material = Material(
                InventoryMenu.BLOCK_ATLAS,
                ExtendedAEPlus.getLocation("block/crafting/$name")
            )
            Materials += material
            return material
        }
    }
}
