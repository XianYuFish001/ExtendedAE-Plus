package com.extendedae_plus.util

import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.networking.security.IActionHost
import appeng.api.stacks.AEKey
import appeng.me.service.CraftingService
import appeng.menu.AEBaseMenu
import com.fish.fishlib.util.extension.tryCast
import net.minecraft.server.level.ServerPlayer

object UtilNetwork {
    fun findProviders(player: ServerPlayer, key: AEKey) = ArrayList<ICraftingProvider>().apply {
        val serviceCrafting = player.containerMenu.tryCast<AEBaseMenu>()
            ?.target?.tryCast<IActionHost>()
            ?.actionableNode
            ?.grid
            ?.craftingService?.tryCast<CraftingService>()
            ?: return@apply

        serviceCrafting.getCraftingFor(key)
            .forEach { this += serviceCrafting.getProviders(it) }
    }
}