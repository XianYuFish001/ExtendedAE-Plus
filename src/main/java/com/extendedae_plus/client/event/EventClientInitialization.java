package com.extendedae_plus.client.event;

import appeng.client.render.crafting.CraftingCubeModel;
import appeng.init.client.InitScreens;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.client.RegistriesBuiltInModel;
import com.extendedae_plus.client.render.crafting.EAEPCraftingCubeModelProvider;
import com.extendedae_plus.client.screen.ScreenPriorityTool;
import com.extendedae_plus.client.screen.ScreenProviderController;
import com.extendedae_plus.client.screen.ScreenTicker;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.dataComponent.DataTickingCard;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModMenuTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 确保在模型烘焙/资源重载期间也会注册内置模型，避免在刷新资源后丢失内置模型映射。
 */
@EventBusSubscriber(modid = ExtendedAEPlus.MODID, value = Dist.CLIENT)
public final class EventClientInitialization {
    private static boolean MODEL_REGISTERED = false;

    @SubscribeEvent
    public static void regScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.PROVIDER_CONTROLLER.get(),
                ScreenProviderController::new);

        InitScreens.register(event, ModMenuTypes.TICKER.get(),
                ScreenTicker::new, getStylePath("ticker"));
        InitScreens.register(event, ModMenuTypes.PRIORITY_TOOL.get(),
                ScreenPriorityTool::new, getStylePath("priority_tool"));
    }

    @SubscribeEvent
    public static void regAdditional(ModelEvent.RegisterAdditional event) {
        regStandaloneModel(event, "block/crafting/accelerator_4x_formed_v2");
        regStandaloneModel(event, "block/crafting/accelerator_16x_formed_v2");
        regStandaloneModel(event, "block/crafting/accelerator_64x_formed_v2");
        regStandaloneModel(event, "block/crafting/accelerator_256x_formed_v2");
        regStandaloneModel(event, "block/crafting/accelerator_1024x_formed_v2");
        initModels();
    }

    public static void initModels() {
        if (MODEL_REGISTERED) return;
        MODEL_REGISTERED = true;

        ItemProperties.register(ModItems.TICKING_CARD.get(), ExtendedAEPlus.getLocation("multiplier"),
                (stack, world, entity, seed) -> DataTickingCard.fromStack(stack).multiplier());

        addCrafterModel("accelerator_4x_formed_v2", EAEPCraftingUnitType.ACCELERATOR_4x);
        addCrafterModel("accelerator_16x_formed_v2", EAEPCraftingUnitType.ACCELERATOR_16x);
        addCrafterModel("accelerator_64x_formed_v2", EAEPCraftingUnitType.ACCELERATOR_64x);
        addCrafterModel("accelerator_256x_formed_v2", EAEPCraftingUnitType.ACCELERATOR_256x);
        addCrafterModel("accelerator_1024x_formed_v2", EAEPCraftingUnitType.ACCELERATOR_1024x);
    }

    private static void regStandaloneModel(ModelEvent.RegisterAdditional event, String location) {
        event.register(ModelResourceLocation.standalone(ExtendedAEPlus.getLocation(location)));
    }

    private static void addCrafterModel(String location, EAEPCraftingUnitType type) {
        RegistriesBuiltInModel.addBuiltInModel(
                ExtendedAEPlus.getLocation("block/crafting/" + location),
                new CraftingCubeModel(new EAEPCraftingCubeModelProvider(type)));
    }

    private static String getStylePath(String fileName) {
        return "/screens/extendedae_plus/" + fileName + ".json";
    }
}
