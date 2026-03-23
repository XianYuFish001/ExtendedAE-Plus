package com.extendedae_plus.client.event

import appeng.api.stacks.GenericStack
import appeng.core.AEConfig
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.client.EAEPKeyMappings
import com.extendedae_plus.integration.impl.recipeViewer.HelperRecipeViewer
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorScreenStorage
import com.extendedae_plus.mixin.core.extendedae.accessor.AccessorScreenExAccess
import com.extendedae_plus.network.CPacketPullFromNetwork
import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToServer
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.ScreenEvent
import org.lwjgl.glfw.GLFW

// TODO Refactor
@EventBusSubscriber(modid = ExtendedAEPlus.MODID, value = [Dist.CLIENT])
object EventScreenActions {
    private var pulled = false

    @SubscribeEvent
    fun onMouseButtonPre(event: InputEvent.MouseButton.Pre) {
        if (Minecraft.getInstance().player == null) return
        if (Minecraft.getInstance().screen == null) return

        if (HelperRecipeViewer.isCheatMode()) return

        if (event.action != GLFW.GLFW_PRESS) {
            if (pulled) event.setCanceled(true)
            pulled = false
            return
        }

        val pulled = HelperRecipeViewer.getPulled(event.button)
        if (pulled.first > 0) {
            val stack = HelperRecipeViewer.getHoveredStacks().firstOrNull() ?: return

            CPacketPullFromNetwork(
                GenericStack(stack.what(), pulled.first.toLong()),
                true,
                pulled.second
            ).sendToServer()
            EventScreenActions.pulled = true
            return
        }

        if (EAEPKeyMappings.TriggerCraft.matchesMouse(event.button)) {
            val stack = HelperRecipeViewer.getHoveredStacks().firstOrNull() ?: return

            CPacketPullFromNetwork(stack, doPull = false, toInventory = false).sendToServer()
            event.setCanceled(true)
        }
    }

    @SubscribeEvent
    fun onKeyPressedPre(event: ScreenEvent.KeyPressed.Pre) {
        if (Minecraft.getInstance().player == null) return
        if (EAEPKeyMappings.FillToSearch.matches(event.keyCode, event.scanCode)) {
            // 仅当鼠标确实悬停在 JEI 配料上时触发
            // 大概会在一格有多个(?)stack的时候出bug, 但是真的会有那种时候吗?
            val stack = HelperRecipeViewer.getHoveredStacks().firstOrNull() ?: return
            val name = stack.what().displayName.string

            if (AEConfig.instance().isUseExternalSearch) {
                HelperRecipeViewer.setSearchText(name)
                event.setCanceled(true)
                return
            } else when (val screen = Minecraft.getInstance().screen) {
                is AccessorScreenStorage -> {
                    screen.fieldSearch.value = name
                    screen.`eaep$setSearchText`(name)
                }

                is AccessorScreenExAccess -> screen.fieldSearch.value = name
            }
            event.setCanceled(true)
        }
    }

    @SubscribeEvent
    fun clearContents(event: ScreenEvent.Closing) {
        pulled = false
    }
}
