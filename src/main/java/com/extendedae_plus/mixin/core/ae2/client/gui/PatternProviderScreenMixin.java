package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.extendedae_plus.mixin.impl.widget.ButtonImplementations;
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
@Mixin(value = PatternProviderScreen.class, remap = false)
public abstract class PatternProviderScreenMixin<C extends PatternProviderMenu>
        extends AEBaseScreen<C>
        implements HelperProviderButtons {
    @Unique
    private EAEPServerCycleButton eaep$buttonSmartBlocking;
    @Unique
    private EAEPServerCycleButton eaep$buttonSmartDoubling;

    @Unique
    public final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;

    public PatternProviderScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void eap$initAdvancedBlocking(C menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        // 初始化后立刻对齐当前@GuiSync状态，避免首帧显示不一致

        this.eaep$buttonSmartBlocking = ButtonImplementations.buttonBlocking(menu);
        this.eaep$buttonSmartDoubling = ButtonImplementations.buttonDoubling(menu);

        this.addToLeftToolbar(this.eaep$buttonSmartBlocking);
        this.addToLeftToolbar(this.eaep$buttonSmartDoubling);

        EAEPActionItems.GROUPED_ACTIONS.get("scaling").forEach(action ->
                this.eaep$scalingButtons.add(new EAEPActionButton(action, CPacketScalePatterns::send)));

        this.eaep$scalingButtons.forEach(button -> {
            this.addRenderableWidget(button);
            button.setVisibility(true);
        });
    }

    // 每帧刷新：仅从菜单(@GuiSync)同步布尔值，保持按钮状态一致
    @Inject(method = "updateBeforeRender", at = @At("HEAD"), remap = false)
    private void eap$updateAdvancedBlocking(CallbackInfo ci) {
        try {
            this.eaep$updateButtonsStates();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public List<EAEPActionButton> eaep$getScalingButtons() {
        return this.eaep$scalingButtons;
    }

    @Override
    public void eaep$updateButtonsStates() {
        this.eaep$buttonSmartBlocking.updateState();
        this.eaep$buttonSmartDoubling.updateState();

        this.eaep$lastScreenInfo = ButtonImplementations.updateScalingButtonsLayout(
                this,
                this.leftPos + this.imageWidth + 3,
                this.topPos + 50,
                this.eaep$lastScreenInfo
        );
    }
}
