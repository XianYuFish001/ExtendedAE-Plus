package com.extendedae_plus.mixin.helper

import appeng.blockentity.AEBaseBlockEntity
import appeng.helpers.patternprovider.PatternProviderLogicHost
import appeng.menu.locator.MenuHostLocator
import appeng.menu.locator.MenuLocators
import appeng.parts.AEBasePart
import net.minecraft.world.entity.player.Player
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost

interface HelperProviderHost {
    val hostVanilla: PatternProviderLogicHost?
        get() = null

    val hostAdv: AdvPatternProviderLogicHost?
        get() = null

    val opener
        get() = opener@{ player: Player, locator: MenuHostLocator? ->
            val host = this.hostVanilla ?: this.hostAdv ?: return@opener
            val locator = locator ?: when (host) {
                is AEBasePart -> MenuLocators.forPart(host)
                is AEBaseBlockEntity -> MenuLocators.forBlockEntity(host)
                else -> return@opener
            }
            this.hostVanilla?.openMenu(player, locator)
            this.hostAdv?.openMenu(player, locator)
        }

    val targets
        get() = this.hostVanilla?.targets
            ?: this.hostAdv?.targets
            ?: mutableSetOf()

    val pos
        get() = (this.hostVanilla?.blockEntity
            ?: this.hostAdv?.blockEntity)
            ?.blockPos

    val targetIcon
        get() = this.hostVanilla?.terminalGroup?.icon()
            ?: this.hostAdv?.terminalGroup?.icon()
}