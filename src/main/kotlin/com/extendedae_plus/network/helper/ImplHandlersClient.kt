package com.extendedae_plus.network.helper

import appeng.client.gui.AEBaseScreen
import appeng.client.gui.me.items.PatternEncodingTermScreen
import appeng.menu.SlotSemantics
import appeng.menu.me.items.PatternEncodingTermMenu
import com.extendedae_plus.client.screen.ScreenProviderList
import com.extendedae_plus.client.screen.WrapperScreenAE
import com.extendedae_plus.client.screen.labelLink.ScreenLabelLink
import com.extendedae_plus.mixin.helper.BridgePlanToEncode
import com.extendedae_plus.network.SPacketLabelList
import com.extendedae_plus.network.SPacketProvidersInfo
import com.extendedae_plus.network.SPacketSetProviderPage
import com.fish.fishlib.network.HandlerClient
import com.fish.fishlib.network.base.SPacketGeneric
import com.glodblock.github.extendedae.client.gui.GuiExPatternProvider
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
    SlotPatternHighlight(HandlerClient@{
        // TODO Refactor
    }),
    LabelList(HandlerClient<SPacketLabelList> {
        val screen = Minecraft.getInstance().screen
        if (screen !is ScreenLabelLink) return@HandlerClient
        screen.setLabels(this.labels)
    }),
    ProvidersInfo(HandlerClient<SPacketProvidersInfo> {
        var screenCurrent: AEBaseScreen<PatternEncodingTermMenu>
        val screenAE: AEBaseScreen<*> = when (val screen = Minecraft.getInstance().screen) {
            is BoMScreen -> {
                val old = screen.old as? AEBaseScreen<*> ?: return@HandlerClient
                val menu = old.menu as? PatternEncodingTermMenu ?: return@HandlerClient
                screenCurrent = WrapperScreenAE(menu, screen)
                old
            }

            is PatternEncodingTermScreen<*> -> {
                // TODO Refactor
                if (screen.getMenu() !is PatternEncodingTermMenu) return@HandlerClient
                screenCurrent = screen as AEBaseScreen<PatternEncodingTermMenu>
                screen
            }

            else -> return@HandlerClient
        }
        screenAE.switchToScreen(
            ScreenProviderList(
                screenCurrent,
                this.info
            )
        )
    }),
    ProviderPage(HandlerClient<SPacketSetProviderPage> {
        // TODO Refactor
        val screen = Minecraft.getInstance().screen
        if (screen is GuiExPatternProvider) {
            val currentPage = screen.javaClass.getDeclaredField("eap\$currentPage")
            currentPage.setAccessible(true)
            currentPage.setInt(screen, this.page)

            screen.repositionSlots(SlotSemantics.ENCODED_PATTERN)
            screen.repositionSlots(SlotSemantics.STORAGE)

            val hs = screen.javaClass.getDeclaredField("hoveredSlot")
            hs.setAccessible(true)
            hs.set(screen, null)
        }
    })

    ;

    override operator fun invoke() = this.handler
}

