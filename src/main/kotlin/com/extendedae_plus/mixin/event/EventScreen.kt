package com.extendedae_plus.mixin.event

import com.fish.fishlib.util.extension.cast
import net.minecraft.client.gui.screens.Screen
import org.jetbrains.annotations.ApiStatus

object EventScreen {
    @ApiStatus.Internal
    @Suppress("FunctionName")
    interface AccessorEvent {
        fun `eaep$ticker`(): (Screen) -> Unit

        @JvmSuppressWildcards
        fun `eaep$ticker`(ticker: (Screen) -> Unit)
    }

    @JvmStatic
    fun <T : Screen> T.ticker(ticker: (T) -> Unit) {
        if (this !is AccessorEvent) return
        val tickerExisting = this.`eaep$ticker`()
        this.`eaep$ticker` {
            tickerExisting(it)
            ticker(it.cast())
        }
    }
}