package com.extendedae_plus.client.render.widgets.button

import appeng.api.config.YesNo
import appeng.client.gui.AEBaseScreen
import appeng.core.network.serverbound.ConfigButtonPacket
import com.extendedae_plus.common.init.EAEPSettings
import com.extendedae_plus.common.registry.settings.StateSmartBlocking
import com.extendedae_plus.mixin.helper.HelperProviderButtons
import com.extendedae_plus.mixin.helper.SyncerSmartBlocking
import com.extendedae_plus.mixin.helper.SyncerSmartDoubling
import com.extendedae_plus.mixin.impl.HelperRenderablesModifier
import com.mojang.datafixers.util.Pair
import net.minecraft.world.inventory.AbstractContainerMenu

object ButtonImplementations {
    @JvmStatic
    fun buttonBlocking(menu: AbstractContainerMenu): EAEPServerCycleButton {
        val button = EAEPServerCycleButton.Builder()
            .setTask(ConfigButtonPacket(EAEPSettings.smartBlocking, false))
            .addPart(EAEPActionItems.BlockingDisabled)
            .addPart(EAEPActionItems.BlockingEnabled)
            .addPart(EAEPActionItems.BlockingUnable)
            .setIterator { prev, reversed -> (prev + (if (reversed) -1 else 1)) % 2 }
            .setSyncer {
                if (menu !is SyncerSmartBlocking) return@setSyncer 0
                else return@setSyncer when (menu.`eaep$getBlockingState`()) {
                    StateSmartBlocking.DISABLED -> 0
                    StateSmartBlocking.ENABLED -> 1
                    StateSmartBlocking.DISABLED_BY_SUPER -> 2
                }
            }
            .build()
        button.updateState()
        return button
    }

    @JvmStatic
    fun buttonDoubling(menu: AbstractContainerMenu): EAEPServerCycleButton {
        val button = EAEPServerCycleButton.Builder()
            .setTask(ConfigButtonPacket(EAEPSettings.smartDoubling, false))
            .addPart(EAEPActionItems.DoublingDisabled)
            .addPart(EAEPActionItems.DoublingEnabled)
            .setSyncer(SyncerBooleanGeneric {
                menu is SyncerSmartDoubling
                        && YesNo.YES == menu.`eaep$getDoublingState`()
            })
            .build()
        button.updateState()
        return button
    }

    @JvmStatic
    fun <T : AEBaseScreen<*>> updateScalingButtonsLayout(
        screen: T,
        bx: Int,
        by: Int,
        avoidToolbox: Boolean,
        lastScreenInfo: Pair<Int, Int>?
    ): Pair<Int, Int> {
        var lastScreenInfo = lastScreenInfo
        if (screen !is HelperProviderButtons) return Pair(0, 0)

        val flagReplaceButton = lastScreenInfo == null
                    || screen.width != lastScreenInfo.first
                    || screen.height != lastScreenInfo.second
        if (flagReplaceButton)
            lastScreenInfo = Pair(screen.width, screen.height)

        val buttons = screen.`eaep$getButtons`()
        val spacing: Int = buttons[0].height + 6
        buttons.forEach { button ->
            button.setVisibility(true)
            if (!screen.renderables.contains(button))
                HelperRenderablesModifier.addRenderableWidget(screen, button)

            if (flagReplaceButton) {
                HelperRenderablesModifier.removeWidget(screen, button)
                HelperRenderablesModifier.addRenderableWidget(screen, button)
            }

            val indexButton = buttons.indexOf(button)
            button.x = bx + (if (avoidToolbox && indexButton >= 3) spacing else 0)
            button.y = by + spacing * (if (avoidToolbox) indexButton % 3 else indexButton)
        }

        return lastScreenInfo
    }
}
