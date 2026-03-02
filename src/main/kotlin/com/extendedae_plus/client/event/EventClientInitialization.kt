package com.extendedae_plus.client.event

import appeng.client.render.crafting.CraftingCubeModel
import appeng.hooks.BuiltInModelHooks
import appeng.init.client.InitScreens
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.client.render.crafting.EAEPCraftingCubeModelProvider
import com.extendedae_plus.client.screen.ScreenPriorityTool
import com.extendedae_plus.client.screen.ScreenTicker
import com.extendedae_plus.client.screen.labelLink.ScreenLabelLink
import com.extendedae_plus.client.screen.labelLink.ScreenLabelLinkManageable
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.extendedae_plus.common.registry.block.EAEPCraftingUnit
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard
import com.fish.fishlib.common.InitObject
import com.fish.fishlib.util.extension.invoke
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.client.resources.model.ModelResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

/**
 * 确保在模型烘焙/资源重载期间也会注册内置模型，避免在刷新资源后丢失内置模型映射。
 */
@EventBusSubscriber(modid = ExtendedAEPlus.MODID, value = [Dist.CLIENT])
object EventClientInitialization {
    @SubscribeEvent
    fun regScreens(event: RegisterMenuScreensEvent) {
        InitScreens.register(
            event,
            EAEPMenuTypes.Ticker(),
            ::ScreenTicker,
            "ticker".style()
        )
        InitScreens.register(
            event,
            EAEPMenuTypes.PriorityTool(),
            ::ScreenPriorityTool,
            "priority_tool".style()
        )
        InitScreens.register(
            event,
            EAEPMenuTypes.LabelLink(),
            ::ScreenLabelLink,
            "label_link".style()
        )
        InitScreens.register(
            event,
            EAEPMenuTypes.LabelLinkManageable(),
            ::ScreenLabelLinkManageable,
            "label_link_manageable".style()
        )
    }

    @SubscribeEvent
    fun initModels(event: ModelEvent.RegisterAdditional) {
        event.standalone("block/crafting/accelerator_4x_formed_v2")
        event.standalone("block/crafting/accelerator_16x_formed_v2")
        event.standalone("block/crafting/accelerator_64x_formed_v2")
        event.standalone("block/crafting/accelerator_256x_formed_v2")
        event.standalone("block/crafting/accelerator_1024x_formed_v2")

        ItemProperties.register(
            EAEPItems.CardTicking(),
            ExtendedAEPlus.getLocation("multiplier")
        ) { stack, _, _, _ ->
            DataTickingCard.fromStack(stack).multiplier.toFloat()
        }
    }

    @InitObject(dist = [Dist.CLIENT])
    fun bindModels() {
        this.crafterModel("accelerator_4x_formed_v2", EAEPCraftingUnit.ACCELERATOR_4x)
        this.crafterModel("accelerator_16x_formed_v2", EAEPCraftingUnit.ACCELERATOR_16x)
        this.crafterModel("accelerator_64x_formed_v2", EAEPCraftingUnit.ACCELERATOR_64x)
        this.crafterModel("accelerator_256x_formed_v2", EAEPCraftingUnit.ACCELERATOR_256x)
        this.crafterModel("accelerator_1024x_formed_v2", EAEPCraftingUnit.ACCELERATOR_1024x)
    }

    private fun ModelEvent.RegisterAdditional.standalone(location: String) {
        register(ModelResourceLocation.standalone(ExtendedAEPlus.getLocation(location)))
    }

    private fun crafterModel(location: String, type: EAEPCraftingUnit) = BuiltInModelHooks.addBuiltInModel(
        ExtendedAEPlus.getLocation("block/crafting/$location"),
        CraftingCubeModel(EAEPCraftingCubeModelProvider(type))
    )

    private fun String.style() = "/screens/extendedae_plus/$this.json"
}
