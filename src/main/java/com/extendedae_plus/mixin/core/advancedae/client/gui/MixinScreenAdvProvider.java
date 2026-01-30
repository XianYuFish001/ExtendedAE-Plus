package com.extendedae_plus.mixin.core.advancedae.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.extendedae_plus.client.render.widgets.button.*;
import com.extendedae_plus.mixin.bridge.HelperProviderButtons;
import com.extendedae_plus.network.CPacketScalePatterns;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.pedroksl.advanced_ae.client.gui.AdvPatternProviderScreen;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AdvPatternProviderScreen.class)
public abstract class MixinScreenAdvProvider extends AEBaseScreen<AdvPatternProviderMenu> implements HelperProviderButtons {
    @Unique
    private EAEPServerCycleButton eaep$buttonSmartBlocking;
    @Unique
    private EAEPServerCycleButton eaep$buttonSmartDoubling;
    @Unique
    private final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;

    public MixinScreenAdvProvider(AdvPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(AdvPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        this.eaep$buttonSmartBlocking = ButtonImplementations.buttonBlocking(menu);
        this.eaep$buttonSmartDoubling = ButtonImplementations.buttonDoubling(menu);
        this.addToLeftToolbar(this.eaep$buttonSmartBlocking);
        this.addToLeftToolbar(this.eaep$buttonSmartDoubling);

        EAEPActionItems.actions.get("scaling").forEach(action ->
                this.eaep$scalingButtons.add(new EAEPActionButton(action, CPacketScalePatterns::send)));
        this.eaep$scalingButtons.forEach(button -> {
            this.addRenderableWidget(button);
            button.setVisibility(true);
        });
    }

    @Inject(method = "updateBeforeRender", at = @At("HEAD"), remap = false)
    private void update(CallbackInfo ci) {
        this.eaep$updateButtonsStates();
    }

    @Override
    public void eaep$updateButtonsStates() {
        this.eaep$buttonSmartBlocking.updateState();
        this.eaep$buttonSmartDoubling.updateState();

        this.eaep$lastScreenInfo = ButtonImplementations.updateScalingButtonsLayout(
                this,
                this.leftPos + this.imageWidth + 3,
                this.topPos + 68,
                false,
                this.eaep$lastScreenInfo
        );
    }

    @Override
    public List<? extends EAEPButton> eaep$getButtons() {
        return this.eaep$scalingButtons;
    }
}
