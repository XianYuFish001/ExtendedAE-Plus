package com.extendedae_plus.dataGen;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.init.ModItems;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.extendedae.util.EAETags;
import me.ramidzkh.mekae2.data.RecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;

public class Recipe extends RecipeProvider {
    public Recipe(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CHANNEL_CARD)
                .requires(AEItems.ADVANCED_CARD)
                .requires(AEItems.SINGULARITY)
                .requires(AEItems.FLUIX_PEARL)
                .unlockedBy("has_card", has(AEItems.ADVANCED_CARD))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PROVIDER_CONTROLLER)
                .requires(AEItems.NETWORK_TOOL)
                .requires(EAETags.EX_PATTERN_PROVIDER)
                .unlockedBy("has_provider", has(EAETags.EX_PATTERN_PROVIDER))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, EAEPCraftingUnitType.ACCELERATOR_4x.getBlock())
                .requires(AEBlocks.CRAFTING_ACCELERATOR)
                .requires(AEItems.CELL_COMPONENT_4K)
                .unlockedBy("has_accelerator", has(AEBlocks.CRAFTING_ACCELERATOR))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, EAEPCraftingUnitType.ACCELERATOR_16x.getBlock())
                .requires(AEBlocks.CRAFTING_ACCELERATOR)
                .requires(AEItems.CELL_COMPONENT_16K)
                .unlockedBy("has_accelerator", has(AEBlocks.CRAFTING_ACCELERATOR))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, EAEPCraftingUnitType.ACCELERATOR_64x.getBlock())
                .requires(AEBlocks.CRAFTING_ACCELERATOR)
                .requires(AEItems.CELL_COMPONENT_64K)
                .unlockedBy("has_accelerator", has(AEBlocks.CRAFTING_ACCELERATOR))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, EAEPCraftingUnitType.ACCELERATOR_256x.getBlock())
                .requires(AEBlocks.CRAFTING_ACCELERATOR)
                .requires(AEItems.CELL_COMPONENT_256K)
                .unlockedBy("has_accelerator", has(AEBlocks.CRAFTING_ACCELERATOR))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, EAEPCraftingUnitType.ACCELERATOR_1024x.getBlock())
                .requires(AEBlocks.CRAFTING_ACCELERATOR)
                .requires(AEItems.CELL_COMPONENT_256K, 2)
                .unlockedBy("has_accelerator", has(AEBlocks.CRAFTING_ACCELERATOR))
                .save(recipeOutput);

        // WIP: 这里define传入的物品无法在生成中被应用dataComponents, 先手动改吧
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataSpeedCard.toStack(2))
//                .pattern("aba")
//                .pattern("cdc")
//                .pattern("aba")
//                .define('a', AEItems.SPEED_CARD)
//                .define('b', AEItems.CELL_COMPONENT_16K)
//                .define('c', AEItems.LOGIC_PROCESSOR)
//                .define('d', AEItems.SINGULARITY)
//                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
//                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_2x"));
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataSpeedCard.toStack(4))
//                .pattern("aba")
//                .pattern("cdc")
//                .pattern("aba")
//                .define('a', DataSpeedCard.toIngredient(2))
//                .define('b', AEItems.CELL_COMPONENT_64K)
//                .define('c', AEItems.SPATIAL_2_CELL_COMPONENT)
//                .define('d', AEItems.SINGULARITY)
//                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
//                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_4x"));
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataSpeedCard.toStack(8))
//                .pattern("aba")
//                .pattern("cdc")
//                .pattern("aba")
//                .define('a', DataSpeedCard.toIngredient(4))
//                .define('b', AEItems.CELL_COMPONENT_256K)
//                .define('c', AEItems.SPATIAL_16_CELL_COMPONENT)
//                .define('d', AEItems.SINGULARITY)
//                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
//                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_8x"));
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataSpeedCard.toStack(16))
//                .pattern("aba")
//                .pattern("cdc")
//                .pattern("aba")
//                .define('a', DataSpeedCard.toIngredient(8))
//                .define('b', AEItems.SINGULARITY)
//                .define('c', AEItems.SPATIAL_128_CELL_COMPONENT)
//                .define('d', Items.NETHER_STAR)
//                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
//                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_16x"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ENTITY_TICKER_PART_ITEM)
                .pattern("ses")
                .pattern("apf")
                .pattern("ses")
                .define('s', AEItems.SINGULARITY)
                .define('e', AEBlocks.DENSE_ENERGY_CELL)
                .define('a', AEItems.ANNIHILATION_CORE)
                .define('p', EAESingletons.EX_IO_PORT)
                .define('f', AEItems.FORMATION_CORE)
                .unlockedBy("has_singularity", has(AEItems.SINGULARITY))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WIRELESS_TRANSCEIVER)
                .pattern("rrr")
                .pattern("rlr")
                .pattern("rrr")
                .define('r', AEBlocks.QUANTUM_RING)
                .define('l', AEBlocks.QUANTUM_LINK)
                .unlockedBy("has_singularity", has(AEItems.SINGULARITY))
                .save(recipeOutput);

        CrystalAssemblerRecipeBuilder.assemble(ModItems.ASSEMBLER_MATRIX_UPLOAD_CORE)
                .input(EAESingletons.ASSEMBLER_MATRIX_WALL)
                .input(AEItems.SINGULARITY)
                .input(AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.LIME), 6)
                .input(AEItems.LOGIC_PROCESSOR)
                .save(recipeOutput, ExtendedAEPlus.getLocation("assembler/assembler_matrix_uploader"));
        CrystalAssemblerRecipeBuilder.assemble(ModItems.INFINITY_BIGINTEGER_CELL_ITEM)
                .input(AEItems.SINGULARITY, 64)
                .input(Items.NETHER_STAR, 2)
                .input(AEItems.ITEM_CELL_256K)
                .input(AEItems.FLUID_CELL_256K)
                .fluid(Fluids.LAVA, 2000)
                .save(recipeOutput, ExtendedAEPlus.getLocation("assembler/infinity_cell_item"));
    }
}
