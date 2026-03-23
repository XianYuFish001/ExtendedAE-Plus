package com.extendedae_plus.client

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.settings.IKeyConflictContext
import net.neoforged.neoforge.client.settings.KeyConflictContext
import org.lwjgl.glfw.GLFW

@EventBusSubscriber(modid = ExtendedAEPlus.MODID, value = [Dist.CLIENT])
object EAEPKeyMappings {
    private val Category = UtilKeyBuilder.of(Patterns.KeyCategory).buildRaw()
    private val Mappings = HashSet<Lazy<KeyMapping>>()

    val FillToSearch by register(
        "fill_to_search",
        GLFW.GLFW_KEY_F
    )
    val TriggerCraft by register(
        "trigger_craft",
        GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
        inputType = InputConstants.Type.MOUSE
    )

    private fun register(
        name: String,
        keyCode: Int,
        keyConflictContext: IKeyConflictContext = KeyConflictContext.GUI,
        inputType: InputConstants.Type = InputConstants.Type.KEYSYM,
        category: String = Category
    ) = lazy {
        KeyMapping(
            UtilKeyBuilder.of(Patterns.Key)
                .addStr(name)
                .buildRaw(),
            keyConflictContext,
            inputType,
            keyCode,
            category
        )
    }.also(Mappings::add)

    @SubscribeEvent
    private fun reg(event: RegisterKeyMappingsEvent) = Mappings
        .map(Lazy<KeyMapping>::value)
        .forEach(event::register)
}