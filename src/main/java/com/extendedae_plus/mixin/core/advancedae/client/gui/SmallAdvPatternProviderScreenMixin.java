package com.extendedae_plus.mixin.core.advancedae.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton;
import com.extendedae_plus.mixin.impl.widget.ButtonImplementations;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.pedroksl.advanced_ae.client.gui.SmallAdvPatternProviderScreen;
import net.pedroksl.advanced_ae.gui.advpatternprovider.SmallAdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为高级ae样板供应器界面添加“高级阻挡模式”按钮。
 * - 位于左侧工具栏
 * - 点击仅发送 C2S 切换请求；状态由 AE2 @GuiSync 回传决定
 */
@Mixin(SmallAdvPatternProviderScreen.class)
public abstract class SmallAdvPatternProviderScreenMixin extends AEBaseScreen<SmallAdvPatternProviderMenu> {
    @Unique
    private EAEPServerCycleButton eaep$buttonSmartBlocking;
    @Unique
    private EAEPServerCycleButton eaep$buttonSmartDoubling;

    public SmallAdvPatternProviderScreenMixin(SmallAdvPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(SmallAdvPatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        this.eaep$buttonSmartBlocking = ButtonImplementations.buttonBlocking(menu);
        this.eaep$buttonSmartDoubling = ButtonImplementations.buttonDoubling(menu);
        this.addToLeftToolbar(this.eaep$buttonSmartBlocking);
        this.addToLeftToolbar(this.eaep$buttonSmartDoubling);
    }

    // 每帧刷新：仅从菜单(@GuiSync)同步布尔值，保持按钮状态一致
    @Inject(method = "updateBeforeRender", at = @At("HEAD"), remap = false)
    private void updateButtonsStates(CallbackInfo ci) {
        this.eaep$buttonSmartBlocking.updateState();
        this.eaep$buttonSmartDoubling.updateState();
    }
}
