package com.extendedae_plus.mixin.core.extendedae.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.extendedae_plus.network.CPacketInterfaceScaling;
import com.glodblock.github.extendedae.client.gui.GuiExInterface;
import com.glodblock.github.extendedae.container.ContainerExInterface;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiExInterface.class)
public class MixinExInterfaceScaling extends UpgradeableScreen<ContainerExInterface> implements HelperProviderButtons {
    @Unique
    public final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;

    public MixinExInterfaceScaling(ContainerExInterface menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(ContainerExInterface menu,
                        Inventory playerInventory,
                        Component title,
                        ScreenStyle style,
                        CallbackInfo ci) {
        EAEPActionItems.GROUPED_ACTIONS.get("scaling").forEach(action ->
                this.eaep$scalingButtons.add(new EAEPActionButton(action, CPacketInterfaceScaling::send)));

        this.eaep$scalingButtons.forEach(button -> {
            this.addRenderableWidget(button);
            button.setVisibility(true);
        });
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void updateButtons(CallbackInfo ci) {
        this.eaep$updateButtonsLayout();
    }

    @Override
    public void eaep$updateButtonsLayout() {
        boolean flagReplaceButton = this.eaep$lastScreenInfo == null
                || this.width != this.eaep$lastScreenInfo.getFirst()
                || this.height != this.eaep$lastScreenInfo.getSecond();
        if (flagReplaceButton)
            this.eaep$lastScreenInfo = new Pair<>(this.width, this.height);

        int bx = this.leftPos + this.imageWidth + 3;
        int by = this.topPos + 50;
        int spacing = 22;
        this.eaep$scalingButtons.forEach(button -> {
            if (button == null) return;
            button.setVisibility(true);
            if (!this.renderables.contains(button)) this.addRenderableWidget(button);

            if (flagReplaceButton) {
                this.removeWidget(button);
                this.addRenderableWidget(button);
            }

            button.setX(bx);
            button.setY(by + spacing * this.eaep$scalingButtons.indexOf(button));
        });
    }

    @Override
    public List<EAEPActionButton> eaep$getButtons() {
        return this.eaep$scalingButtons;
    }
}
