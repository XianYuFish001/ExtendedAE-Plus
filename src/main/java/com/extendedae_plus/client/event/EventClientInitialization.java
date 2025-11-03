package com.extendedae_plus.client.event;

import appeng.client.render.crafting.CraftingCubeModel;
import appeng.init.client.InitScreens;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.client.render.crafting.EPlusCraftingCubeModelProvider;
import com.extendedae_plus.client.screen.EntitySpeedTickerScreen;
import com.extendedae_plus.client.screen.GlobalProviderModesScreen;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModMenuTypes;
import com.extendedae_plus.common.item.EntitySpeedCardItem;
import com.extendedae_plus.common.menu.EntitySpeedTickerMenu;
import com.extendedae_plus.util.BuiltInModelUtil;
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
                ModMenuTypes.NETWORK_PATTERN_CONTROLLER.get(),
                GlobalProviderModesScreen::new);

        InitScreens.register(event, ModMenuTypes.ENTITY_TICKER_MENU.get(),
                EntitySpeedTickerScreen<EntitySpeedTickerMenu>::new, "/screens/entity_speed_ticker.json");
    }

    @SubscribeEvent
    public static void regAdditional(ModelEvent.RegisterAdditional event) {
        regStandaloneModel(event, "block/crafting/4x_accelerator_formed_v2");
        regStandaloneModel(event, "block/crafting/16x_accelerator_formed_v2");
        regStandaloneModel(event, "block/crafting/64x_accelerator_formed_v2");
        regStandaloneModel(event, "block/crafting/256x_accelerator_formed_v2");
        regStandaloneModel(event, "block/crafting/1024x_accelerator_formed_v2");
        initModels();
    }

    public static void initModels() {
        if (MODEL_REGISTERED) return;
        MODEL_REGISTERED = true;

        ItemProperties.register(ModItems.ENTITY_SPEED_CARD.get(), ExtendedAEPlus.getLocation("mult"),
                (stack, world, entity, seed) -> (float) EntitySpeedCardItem.readMultiplier(stack));

        addCrafterModel("4x_accelerator_formed_v2", EAEPCraftingUnitType.ACCELERATOR_4x);
        addCrafterModel("16x_accelerator_formed_v2", EAEPCraftingUnitType.ACCELERATOR_16x);
        addCrafterModel("64x_accelerator_formed_v2", EAEPCraftingUnitType.ACCELERATOR_64x);
        addCrafterModel("256x_accelerator_formed_v2", EAEPCraftingUnitType.ACCELERATOR_256x);
        addCrafterModel("1024x_accelerator_formed_v2", EAEPCraftingUnitType.ACCELERATOR_1024x);
    }

    private static void regStandaloneModel(ModelEvent.RegisterAdditional event, String location) {
        event.register(ModelResourceLocation.standalone(ExtendedAEPlus.getLocation(location)));
    }

    private static void addCrafterModel(String location, EAEPCraftingUnitType type) {
        BuiltInModelUtil.addBuiltInModel(
                ExtendedAEPlus.getLocation("block/crafting/" + location),
                new CraftingCubeModel(new EPlusCraftingCubeModelProvider(type)));
    }
}
