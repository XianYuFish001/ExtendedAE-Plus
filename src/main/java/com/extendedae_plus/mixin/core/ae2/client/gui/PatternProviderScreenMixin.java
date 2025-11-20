package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.extendedae_plus.mixin.impl.bridge.PatternProviderMenuAdvancedSync;
import com.extendedae_plus.mixin.impl.bridge.PatternProviderMenuDoublingSync;
import com.extendedae_plus.network.CPacketScalePatterns;
import com.extendedae_plus.network.ToggleAdvancedBlockingC2SPacket;
import com.extendedae_plus.network.ToggleSmartDoublingC2SPacket;
import com.extendedae_plus.util.UtilGetKey;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
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
    private static final Logger eaep$LOGGER = LogUtils.getLogger();

    @Unique
    private SettingToggleButton<YesNo> eap$AdvancedBlockingToggle;
    @Unique
    private boolean eap$AdvancedBlockingEnabled = false;

    @Unique
    private SettingToggleButton<YesNo> eap$SmartDoublingToggle;
    @Unique
    private boolean eap$SmartDoublingEnabled = false;

    @Unique
    public final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;

    public PatternProviderScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void eap$initAdvancedBlocking(C menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        // 使用 @GuiSync 初始化
        try {
            if (menu instanceof PatternProviderMenuAdvancedSync sync) {
                this.eap$AdvancedBlockingEnabled = sync.eap$getAdvancedBlockingSynced();
            }
        } catch (Throwable t) {
            eaep$LOGGER.error("Error initializing advanced sync", t);
        }

        // 使用 SettingToggleButton<YesNo> 的外观（原版图标），但自定义悬停描述为“智能阻挡”
        this.eap$AdvancedBlockingToggle = new SettingToggleButton<>(
                Settings.BLOCKING_MODE,
                this.eap$AdvancedBlockingEnabled ? YesNo.YES : YesNo.NO,
                (btn, backwards) ->
                        PacketDistributor.sendToServer(ToggleAdvancedBlockingC2SPacket.INSTANCE)
        ) {
            @Override
            public List<Component> getTooltipMessage() {
                boolean enabled = eap$AdvancedBlockingEnabled;
                return List.of(new UtilGetKey(UtilGetKey.screenTooltip)
                        .addStr("smart_blocking")
                        .addStr(enabled, "enabled", "disabled")
                        .build());
            }
        };
        // 初始化后立刻对齐当前@GuiSync状态，避免首帧显示不一致
        // debug removed
        this.eap$AdvancedBlockingToggle.set(this.eap$AdvancedBlockingEnabled ? YesNo.YES : YesNo.NO);

        this.addToLeftToolbar(this.eap$AdvancedBlockingToggle);

        // 智能翻倍按钮：与高级阻挡同款样式，点击仅发送C2S，状态由@GuiSync驱动
        try {
            if (menu instanceof PatternProviderMenuDoublingSync sync2) {
                this.eap$SmartDoublingEnabled = sync2.eap$getSmartDoublingSynced();
            }
        } catch (Throwable t) {
            eaep$LOGGER.error("Error initializing smart doubling sync", t);
        }

        this.eap$SmartDoublingToggle = new SettingToggleButton<>(
                Settings.BLOCKING_MODE,
                this.eap$SmartDoublingEnabled ? YesNo.YES : YesNo.NO,
                (btn, backwards) ->
                        PacketDistributor.sendToServer(ToggleSmartDoublingC2SPacket.INSTANCE)
        ) {
            @Override
            public List<Component> getTooltipMessage() {
                boolean enabled = eap$SmartDoublingEnabled;
                return List.of(new UtilGetKey(UtilGetKey.screenTooltip)
                        .addStr("smart_doubling")
                        .addStr(enabled, "enabled", "disabled")
                        .build());
            }
        };

        this.eap$SmartDoublingToggle.set(this.eap$SmartDoublingEnabled ? YesNo.YES : YesNo.NO);
        this.addToLeftToolbar(this.eap$SmartDoublingToggle);

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
        if (this.eap$AdvancedBlockingToggle != null) {
            boolean desired = this.eap$AdvancedBlockingEnabled;
            if (this.menu instanceof PatternProviderMenuAdvancedSync sync) {
                desired = sync.eap$getAdvancedBlockingSynced();
            }
            // debug removed
            this.eap$AdvancedBlockingEnabled = desired;
            this.eap$AdvancedBlockingToggle.set(desired ? YesNo.YES : YesNo.NO);
        }

        if (this.eap$SmartDoublingToggle != null) {
            boolean desired2 = this.eap$SmartDoublingEnabled;
            if (this.menu instanceof PatternProviderMenuDoublingSync sync2) {
                desired2 = sync2.eap$getSmartDoublingSynced();
            }
            // debug removed
            this.eap$SmartDoublingEnabled = desired2;
            this.eap$SmartDoublingToggle.set(desired2 ? YesNo.YES : YesNo.NO);
        }

        try {
            this.eaep$updateButtonsLayout();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public List<EAEPActionButton> eaep$getButtons() {
        return this.eaep$scalingButtons;
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
}
