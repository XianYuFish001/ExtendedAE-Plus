package com.extendedae_plus.network.helper

import appeng.api.crafting.PatternDetailsHelper
import appeng.client.gui.AEBaseScreen
import appeng.client.gui.me.items.PatternEncodingTermScreen
import appeng.menu.me.items.PatternEncodingTermMenu
import appeng.menu.slot.AppEngSlot
import appeng.menu.slot.RestrictedInputSlot
import com.extendedae_plus.client.screen.ScreenProviderList
import com.extendedae_plus.client.screen.WrapperScreenAE
import com.extendedae_plus.client.screen.labelLink.ScreenLabelLink
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorSlotRestricted
import com.extendedae_plus.mixin.helper.BridgePlanToEncode
import com.extendedae_plus.mixin.helper.HelperPatternHighlightable
import com.extendedae_plus.mixin.helper.HelperPatternHighlightable.Companion.drawHighlights
import com.extendedae_plus.mixin.helper.HelperPatternHighlightable.Companion.slots
import com.extendedae_plus.network.SPacketHighlightPatternSlot
import com.extendedae_plus.network.SPacketLabelList
import com.extendedae_plus.network.SPacketProvidersInfo
import com.fish.fishlib.network.HandlerClient
import com.fish.fishlib.network.base.SPacketGeneric
import com.fish.fishlib.util.extension.onlyIf
import com.fish.fishlib.util.extension.tryCast
import dev.emi.emi.screen.BoMScreen
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
enum class ImplHandlersClient(val handler: HandlerClient<out SPacketGeneric>) : HolderHandler {
    EncodeFinished(HandlerClient@{ player ->
        val menu = player.containerMenu
        if (menu !is BridgePlanToEncode) return@HandlerClient
        menu.`eaep$execute`()
    }),
    SlotPatternHighlight(HandlerClient<SPacketHighlightPatternSlot> { player ->
        val screen = Minecraft.getInstance().screen
                as? HelperPatternHighlightable ?: return@HandlerClient

        val slotsMatches = screen.slots.mapNotNull {
            if (it !is AppEngSlot) return@mapNotNull null
            val pattern = PatternDetailsHelper.decodePattern(
                it.item,
                player.level()
            ) ?: return@mapNotNull null
            it.onlyIf { pattern.primaryOutput.what == key }
        }

        val slotsOther = screen.slots
            .asSequence()
            .filter {
                (it is AccessorSlotRestricted
                        && it.typePlacable == RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN)
//                    || it is PatternSlot
//                    || it is net.pedroksl.advanced_ae.client.gui.PatternSlot
            }
            .filterNot(slotsMatches::contains)
            .map { it.x to it.y }

        screen.drawHighlights { guiGraphics ->
            slotsOther.forEach { (x, y) ->
                guiGraphics.fill(
                    x, y,
                    x + 16, y + 16,
                    0x6A000000
                )
            }
        }
    }),
    LabelList(HandlerClient<SPacketLabelList> {
        val screen = Minecraft.getInstance().screen
        if (screen !is ScreenLabelLink) return@HandlerClient
        screen.setLabels(this.labels)
    }),
    ProvidersInfo(HandlerClient<SPacketProvidersInfo> {
        var screenCurrent: AEBaseScreen<out PatternEncodingTermMenu>
        when (val screen = Minecraft.getInstance().screen) {
            is BoMScreen -> {
                val old = screen.old as? AEBaseScreen<*> ?: return@HandlerClient
                val menu = old.menu as? PatternEncodingTermMenu ?: return@HandlerClient
                screenCurrent = WrapperScreenAE(menu, screen)
                old
            }

            is PatternEncodingTermScreen<*> -> {
                if (screen.menu !is PatternEncodingTermMenu) return@HandlerClient
                screenCurrent = screen.tryCast() ?: return@HandlerClient
                screen
            }

            else -> return@HandlerClient
        }.switchToScreen(
            ScreenProviderList(
                screenCurrent,
                this.info
            )
        )
    }),
//    ProviderPage(HandlerClient<SPacketSetProviderPage> {
//        // TODO Refactor
//        val screen = Minecraft.getInstance().screen
//        if (screen is GuiExPatternProvider) {
//            val currentPage = screen.javaClass.getDeclaredField("eap\$currentPage")
//            currentPage.setAccessible(true)
//            currentPage.setInt(screen, this.page)
//
//            screen.repositionSlots(SlotSemantics.ENCODED_PATTERN)
//            screen.repositionSlots(SlotSemantics.STORAGE)
//
//            val hs = screen.javaClass.getDeclaredField("hoveredSlot")
//            hs.setAccessible(true)
//            hs.set(screen, null)
//        }
//    })

    ;

    override operator fun invoke() = this.handler
}

