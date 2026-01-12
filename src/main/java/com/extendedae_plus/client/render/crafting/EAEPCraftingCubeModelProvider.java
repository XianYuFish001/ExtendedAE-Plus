package com.extendedae_plus.client.render.crafting;

import appeng.client.render.crafting.AbstractCraftingUnitModelProvider;
import appeng.client.render.crafting.LightBakedModel;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.registry.block.EAEPCraftingUnitType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * 形成态光照模型。
 */
public class EAEPCraftingCubeModelProvider
        extends AbstractCraftingUnitModelProvider<EAEPCraftingUnitType> {

    public static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());
    private static final List<Material> MATERIALS = new ArrayList<>();

    //将环形边框与基础发光底图放在本模组命名空间
    protected static final Material RING_CORNER = texture("ring_corner");
    protected static final Material RING_SIDE_HOR = texture("ring_side_hor");
    protected static final Material RING_SIDE_VER = texture("ring_side_ver");
    protected static final Material LIGHT_BASE = texture("light_base");

    // 亮面贴图（formed 时使用）
    protected static final HashMap<EAEPCraftingUnitType, Material> UNITS_LIGHT = new HashMap<>();

    static {
        for (var unit : EAEPCraftingUnitType.values()) {
            UNITS_LIGHT.put(unit, texture(unit.getSerializedName() + "_light"));
        }
    }

    public EAEPCraftingCubeModelProvider(EAEPCraftingUnitType type) {
        super(type);
    }

    @Override
    public List<Material> getMaterials() {
        return Collections.unmodifiableList(MATERIALS);
    }

    @Override
    public BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite ringCorner = spriteGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = spriteGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = spriteGetter.apply(RING_SIDE_VER);

        return new LightBakedModel(ringCorner, ringSideHor, ringSideVer,
                spriteGetter.apply(LIGHT_BASE), this.getLightMaterial(spriteGetter)) {
            public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
                return CUTOUT;
            }
        };
    }

    private TextureAtlasSprite getLightMaterial(Function<Material, TextureAtlasSprite> textureGetter) {
        return textureGetter.apply(UNITS_LIGHT.get(this.type));
    }

    private static Material texture(String name) {
        var mat = new Material(TextureAtlas.LOCATION_BLOCKS,
                ExtendedAEPlus.getLocation("block/crafting/" + name));
        MATERIALS.add(mat);
        return mat;
    }
}
