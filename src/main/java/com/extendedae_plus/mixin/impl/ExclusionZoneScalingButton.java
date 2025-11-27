package com.extendedae_plus.mixin.impl;

import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;

public class ExclusionZoneScalingButton implements EmiExclusionArea<Screen> {
    @Override
    public void addExclusionArea(Screen screen, Consumer<Bounds> consumer) {
        if (!(screen instanceof HelperProviderButtons helper)) return;

        var buttons = helper.eaep$getButtons();
        if (buttons.isEmpty()) return;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (var button : buttons) {
            if (button.getAction() == null || !"scaling".equals(button.getAction().getGroup())) continue;

            int buttonX = button.getX();
            int buttonY = button.getY();
            int buttonWidth = button.getWidth();
            int buttonHeight = button.getHeight();

            minX = Math.min(minX, buttonX);
            minY = Math.min(minY, buttonY);
            maxX = Math.max(maxX, buttonX + buttonWidth);
            maxY = Math.max(maxY, buttonY + buttonHeight);
        }

        int width = maxX - minX;
        int height = maxY - minY;
        consumer.accept(new Bounds(minX, minY, width, height));
    }
}
