package com.extendedae_plus.mixin.impl

import com.extendedae_plus.mixin.helper.HelperProviderButtons
import dev.emi.emi.api.EmiExclusionArea
import dev.emi.emi.api.widget.Bounds
import net.minecraft.client.gui.screens.Screen
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

object ExclusionZoneScalingButton : EmiExclusionArea<Screen> {
    override fun addExclusionArea(screen: Screen, consumer: Consumer<Bounds>) {
        if (screen !is HelperProviderButtons) return

        val buttons = screen.`eaep$getButtons`()
        if (buttons.isEmpty()) return

        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE

        for (button in buttons) {
            if (button.action == null || "scaling" != button.action?.group) continue

            val buttonX = button.x
            val buttonY = button.y

            minX = min(minX, buttonX)
            minY = min(minY, buttonY)
            maxX = max(maxX, buttonX + button.width)
            maxY = max(maxY, buttonY + button.height)
        }

        val width = maxX - minX
        val height = maxY - minY
        consumer.accept(Bounds(minX, minY, width, height))
    }
}
