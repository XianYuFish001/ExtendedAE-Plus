package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.InterfaceMenu;
import com.extendedae_plus.client.render.widgets.button.ButtonImplementations;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.mixin.bridge.HelperProviderButtons;
import com.extendedae_plus.network.CPacketInterfaceScaling;
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

/**
 * 在 AE2 的 ME 接口界面注入倍增/除法按钮（x2/÷2、x5/÷5、x10/÷10）。
 * 点击时通过 NeoForge 自定义负载发送到服务端调整配置数量。
 */
@Mixin(InterfaceScreen.class)
public abstract class MixinScreenInterface<TMenu extends InterfaceMenu>
        extends AEBaseScreen<TMenu>
        implements HelperProviderButtons {
    @Unique
    public final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;
    @Unique
    private boolean eaep$toolboxAvailable;

    public MixinScreenInterface(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addScaleButtons(CallbackInfo ci) {
        EAEPActionItems.actions.get("scaling").forEach(action ->
                this.eaep$scalingButtons.add(new EAEPActionButton(action, CPacketInterfaceScaling::send)));

        this.eaep$toolboxAvailable = this.menu.getToolbox().isPresent();

        this.eaep$scalingButtons.forEach(button -> {
            this.addRenderableWidget(button);
            button.setVisibility(true);
        });
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void eap$ensureButtons(CallbackInfo ci) {
        this.eaep$updateButtonsStates();
    }

    @Override
    public void eaep$updateButtonsStates() {
        this.eaep$lastScreenInfo = ButtonImplementations.updateScalingButtonsLayout(
                this,
                this.leftPos + this.imageWidth + 3,
                this.topPos + 50,
                this.eaep$toolboxAvailable,
                this.eaep$lastScreenInfo
        );
    }

    @Override
    public List<EAEPActionButton> eaep$getButtons() {
        return this.eaep$scalingButtons;
    }
}
