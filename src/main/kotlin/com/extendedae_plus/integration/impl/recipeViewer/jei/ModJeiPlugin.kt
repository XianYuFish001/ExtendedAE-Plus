package com.extendedae_plus.integration.impl.recipeViewer.jei

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.registry.dataComponent.DataTickingCard
import com.fish.fishlib.util.extension.invoke
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter
import mezz.jei.api.ingredients.subtypes.UidContext
import mezz.jei.api.registration.ISubtypeRegistration
import mezz.jei.api.runtime.IJeiRuntime
import net.minecraft.world.item.ItemStack

@JeiPlugin
class ModJeiPlugin : IModPlugin {
    override fun getPluginUid() = UID

    override fun onRuntimeAvailable(runtime: IJeiRuntime) {
        ViewerJei.runtime = runtime
    }

    override fun registerItemSubtypes(registration: ISubtypeRegistration) {
        registration.registerSubtypeInterpreter(
            VanillaTypes.ITEM_STACK,
            EAEPItems.CardTicking(),
            object : ISubtypeInterpreter<ItemStack> {
                override fun getSubtypeData(ingredient: ItemStack, context: UidContext) =
                    DataTickingCard.fromStack(ingredient)

                @Deprecated("Deprecated in Java")
                override fun getLegacyStringSubtypeInfo(ingredient: ItemStack, context: UidContext) =
                    DataTickingCard.fromStack(ingredient).toString()
            }
        )
    }

    companion object {
        private val UID = ExtendedAEPlus.getLocation("jei_plugin")
    }
}
