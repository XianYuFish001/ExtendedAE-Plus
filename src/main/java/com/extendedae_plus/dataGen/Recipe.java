package com.extendedae_plus.dataGen;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.registry.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard;
import com.extendedae_plus.util.extension.ExtensionBuilderRecipe;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.extendedae.util.EAETags;
import lombok.experimental.ExtensionMethod;
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

@ExtensionMethod(value = ExtensionBuilderRecipe.class, suppressBaseMethods = false)
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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CARD_AUTO_COMPLETION)
                .requires(AEItems.ADVANCED_CARD)
                .requires(Items.CRAFTER)
                .unlockedBy("has_card", has(AEItems.ADVANCED_CARD))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PRIORITY_TOOL)
                .requires(AEItems.MEMORY_CARD)
                .requires(AEItems.ENGINEERING_PROCESSOR)
                .unlockedBy("has_memory_card", has(AEItems.MEMORY_CARD))
                .save(recipeOutput);

        // NeoForged我们喜欢你, 我们喜欢Tags, NeoForgeRegistries, DataComponentIngredient😋
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataTickingCard.toStack(2, 16))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', AEItems.SPEED_CARD)
                .define('b', AEItems.CELL_COMPONENT_16K)
                .define('c', AEItems.LOGIC_PROCESSOR)
                .define('d', AEItems.SINGULARITY)
                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_2x"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataTickingCard.toStack(4, 192))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', DataTickingCard.toIngredient(2, 16))
                .define('b', AEItems.CELL_COMPONENT_64K)
                .define('c', AEItems.SPATIAL_2_CELL_COMPONENT)
                .define('d', AEItems.SINGULARITY)
                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_4x"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataTickingCard.toStack(8, 512))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', DataTickingCard.toIngredient(4, 192))
                .define('b', AEItems.CELL_COMPONENT_256K)
                .define('c', AEItems.SPATIAL_16_CELL_COMPONENT)
                .define('d', AEItems.SINGULARITY)
                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_8x"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DataTickingCard.toStack(16, 1024))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .define('a', DataTickingCard.toIngredient(8, 512))
                .define('b', AEItems.SINGULARITY)
                .define('c', AEItems.SPATIAL_128_CELL_COMPONENT)
                .define('d', Items.NETHER_STAR)
                .unlockedBy("has_card", has(AEItems.SPEED_CARD))
                .save(recipeOutput, ExtendedAEPlus.getLocation("entity_speed_card_16x"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PART_TICKER)
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
                .pattern(" r ")
                .pattern("rlr")
                .pattern(" r ")
                .define('r', AEBlocks.QUANTUM_RING)
                .define('l', AEBlocks.QUANTUM_LINK)
                .unlockedBy("has_singularity", has(AEItems.SINGULARITY))
                .save(recipeOutput);

        CrystalAssemblerRecipeBuilder.assemble(ModItems.PORT_UPLOAD)
                .input(EAESingletons.ASSEMBLER_MATRIX_WALL)
                .input(ModItems.WIRELESS_TRANSCEIVER)
                .input(AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.LIME), 6)
                .input(AEItems.CALCULATION_PROCESSOR)
                .save(recipeOutput, ExtendedAEPlus.getLocation("assembler/assembler_matrix_upload"));
        CrystalAssemblerRecipeBuilder.assemble(ModItems.INFINITY_BIGINTEGER_CELL_ITEM)
                .input(AEItems.SINGULARITY, 64)
                .input(Items.NETHER_STAR, 2)
                .input(AEItems.ITEM_CELL_256K)
                .input(AEItems.FLUID_CELL_256K)
                .fluid(Fluids.LAVA, 2000)
                .save(recipeOutput, ExtendedAEPlus.getLocation("assembler/infinity_cell_item"));
        CrystalAssemblerRecipeBuilder.assemble(ModItems.CORE_ADVANCED_CRAFTER)
                .input(EAESingletons.ASSEMBLER_MATRIX_CRAFTER, 6)
                .input(EAESingletons.EX_ASSEMBLER, 8)
                .input(AEItems.LOGIC_PROCESSOR, 6)
                .input(AEItems.SINGULARITY, 6)
                .input(Items.PURPLE_DYE, 4)
                .save(recipeOutput, ExtendedAEPlus.getLocation("assembler/core_advanced_crafter"));
        CrystalAssemblerRecipeBuilder.assemble(ModItems.CORE_ADVANCED_PATTERN)
                .input(EAESingletons.ASSEMBLER_MATRIX_PATTERN, 6)
                .input(EAETags.EX_PATTERN_PROVIDER, 8)
                .input(AEItems.ENGINEERING_PROCESSOR, 6)
                .input(AEItems.SINGULARITY, 6)
                .input(Items.BLUE_DYE, 4)
                .save(recipeOutput, ExtendedAEPlus.getLocation("assembler/core_advanced_pattern"));
        CrystalAssemblerRecipeBuilder.assemble(ModItems.CORE_ADVANCED_SPEED)
                .input(EAESingletons.ASSEMBLER_MATRIX_SPEED, 6)
                .input(DataTickingCard.toIngredient(2, 16), 8)
                .input(EAESingletons.CONCURRENT_PROCESSOR, 6)
                .input(AEItems.SINGULARITY, 6)
                .input(Items.RED_DYE, 4)
                .save(recipeOutput, ExtendedAEPlus.getLocation("assembler/core_advanced_speed"));
    }
}
