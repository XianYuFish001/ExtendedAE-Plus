package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.client.render.widgets.button.*;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.extendedae_plus.network.CPacketScalePatterns;
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
 * 为 AE2 原版样板供应器界面添加“智能阻挡模式”按钮。
 * - 位于左侧工具栏
 * - 点击仅发送 C2S 切换请求；状态由 AE2 @GuiSync 回传决定
 */
@MixinDependencies(conflict = "expandedae")
@Mixin(value = PatternProviderScreen.class, remap = false)
public abstract class PatternProviderScreenMixin<C extends PatternProviderMenu>
        extends AEBaseScreen<C>
        implements HelperProviderButtons {
    @Unique
    protected EAEPServerCycleButton eaep$buttonSmartBlocking;
    @Unique
    protected EAEPServerCycleButton eaep$buttonSmartDoubling;

    @Unique
    public final List<EAEPButton> eaep$buttons = new ArrayList<>();
    @Unique
    public final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;

    public PatternProviderScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void onInit(C menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        // 初始化后立刻对齐当前@GuiSync状态，避免首帧显示不一致

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

    // 每帧刷新：仅从菜单(@GuiSync)同步布尔值，保持按钮状态一致
    @Inject(method = "updateBeforeRender", at = @At("HEAD"), remap = false)
    private void updateBeforeRender(CallbackInfo ci) {
        try {
            this.eaep$updateButtonsStates();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public List<? extends EAEPButton> eaep$getButtons() {
        if (this.eaep$buttons.isEmpty()) {
            this.eaep$buttons.addAll(this.eaep$scalingButtons);
            this.eaep$buttons.add(this.eaep$buttonSmartBlocking);
            this.eaep$buttons.add(this.eaep$buttonSmartDoubling);
        }
        return this.eaep$buttons;
    }

    @Override
    public void eaep$updateButtonsStates() {
        this.eaep$buttonSmartBlocking.updateState();
        this.eaep$buttonSmartDoubling.updateState();

        this.eaep$lastScreenInfo = ButtonImplementations.updateScalingButtonsLayout(
                this,
                this.leftPos + this.imageWidth + 3,
                this.topPos + 68,
                this.eaep$lastScreenInfo
        );
    }
}
