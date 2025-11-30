package com.extendedae_plus.mixin.core.extendedae.client.gui;

import appeng.client.gui.Icon;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.client.render.widgets.button.ButtonImplementations;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton;
import com.extendedae_plus.mixin.impl.bridge.ExPatternPageAccessor;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.glodblock.github.extendedae.client.button.ActionEPPButton;
import com.glodblock.github.extendedae.client.gui.GuiExPatternProvider;
import com.glodblock.github.extendedae.container.ContainerExPatternProvider;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = GuiExPatternProvider.class, remap = false)
public abstract class GuiExPatternProviderMixin extends PatternProviderScreen<ContainerExPatternProvider> implements HelperProviderButtons, ExPatternPageAccessor {
    @Unique
    private static final Logger eaep$LOGGER = LogUtils.getLogger();
    
    @Unique
    ScreenStyle eap$screenStyle;

    // 不再使用右侧 VerticalButtonBar，直接把按钮注册为独立 AE2 小部件

    @Unique
    private static final int SLOTS_PER_PAGE = 36; // 每页显示36个槽位

    @Unique
    private int eap$currentPage = 0;

    @Unique
    private int eap$maxPageLocal = 1;

    public GuiExPatternProviderMixin(ContainerExPatternProvider menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }



    // 移除手动挪动 Slot 坐标，交由 SlotGridLayout + 原生布局控制

    @Unique
    private int getCurrentPage() {
        // 优先使用本地 GUI 维护的页码
        return Math.max(0, eap$currentPage % Math.max(1, eap$maxPageLocal));
    }

    @Unique
    private int getMaxPage() {
        // 优先使用配置倍数
        try {
            int cfg = EAEPConfig.PAGE_MULTIPLIER.get();
            if (cfg > 1) return cfg;
        } catch (Throwable ignored) {}
        try {
            ContainerExPatternProvider menu1 = this.getMenu();
            Field fieldMaxPage = eap$findFieldRecursive(menu1.getClass(), "maxPage");
            if (fieldMaxPage != null) {
                fieldMaxPage.setAccessible(true);
                Object v = fieldMaxPage.get(menu1);
                if (v instanceof Integer i) return i;
            }
        } catch (Throwable ignored) {}
        // 回退：用槽位总数计算
        try {
            int totalSlots = this.getMenu().getSlots(SlotSemantics.ENCODED_PATTERN).size();
            return Math.max(1, (int) Math.ceil(totalSlots / (double) SLOTS_PER_PAGE));
        } catch (Throwable ignored) {}
        return 1;
    }

    @Unique
    private static Field eap$findFieldRecursive(Class<?> cls, String name) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        return null;
    }

    public ActionEPPButton nextPage;
    public ActionEPPButton prevPage;

    @Unique
    public final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;
    
    // 在构造器返回后初始化按钮与翻页控制
    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectInit(ContainerExPatternProvider menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        this.eap$screenStyle = style;
        // 保留：不再打印菜单类型

        // 计算并下发 maxPage（配置优先，其次按槽位总数计算）
        int totalSlots = this.getMenu().getSlots(SlotSemantics.ENCODED_PATTERN).size();
        int cfgPages = 1;
        try { cfgPages = Math.max(1, EAEPConfig.PAGE_MULTIPLIER.get()); } catch (Throwable ignored) {}
        int calcPages = Math.max(1, (int) Math.ceil(totalSlots / (double) SLOTS_PER_PAGE));
        int desiredMaxPage = Math.max(cfgPages, calcPages);
        eaep$LOGGER.info("[EAEP] GuiExPatternProvider init: totalSlots={}, cfgPages={}, calcPages={}, desiredMaxPage={}", totalSlots, cfgPages, calcPages, desiredMaxPage);
        // 更新本地最大页
        this.eap$maxPageLocal = Math.max(1, desiredMaxPage);
        this.eap$currentPage = 0;
        try {
            Field fMax = eap$findFieldRecursive(menu.getClass(), "maxPage");
            if (fMax != null) { fMax.setAccessible(true); fMax.set(menu, desiredMaxPage); }
        } catch (Throwable ignored) {}

        // 翻页按钮（当存在多页时显示；支持仅由配置决定的“空白页”）
        if (desiredMaxPage > 1) {
            this.prevPage = new ActionEPPButton((b) -> {
                int currentPage = getCurrentPage();
                int maxPage = Math.max(this.eap$maxPageLocal, getMaxPage());
                int newPage = (currentPage - 1 + maxPage) % maxPage;
                try {
                    ContainerExPatternProvider menu1 = this.getMenu();
                    // 尝试调用 setPage
                    try {
                        Method setPageMethod = menu1.getClass().getMethod("setPage", int.class);
                        setPageMethod.invoke(menu1, newPage);
                    } catch (Throwable ignored2) {}
                    // 直接写入 page 字段，确保生效
                    Field f = eap$findFieldRecursive(menu1.getClass(), "page");
                    if (f != null) {
                        f.setAccessible(true);
                        f.set(menu1, newPage);
                    }
                } catch (Exception ignored) {}
                // 同步到本地 GUI 页码
                this.eap$currentPage = newPage;
                // 日志与强制重排（放在更新本地页码之后，确保布局读取到新页）
                eaep$LOGGER.info("[EAEP] PrevPage clicked: {} -> {} (max={})", currentPage, newPage, maxPage);
                this.repositionSlots(SlotSemantics.ENCODED_PATTERN);
                this.repositionSlots(SlotSemantics.STORAGE);
                this.hoveredSlot = null;
                // 更新当前页可见状态
                eap$updatePageSlotActivity();
            }, Icon.ARROW_LEFT);

            this.nextPage = new ActionEPPButton((b) -> {
                int currentPage = getCurrentPage();
                int maxPage = Math.max(this.eap$maxPageLocal, getMaxPage());
                int newPage = (currentPage + 1) % maxPage;
                try {
                    ContainerExPatternProvider menu1 = this.getMenu();
                    // 尝试调用 setPage
                    try {
                        java.lang.reflect.Method setPageMethod = menu1.getClass().getMethod("setPage", int.class);
                        setPageMethod.invoke(menu1, newPage);
                    } catch (Throwable ignored2) {}
                    // 直接写入 page 字段，确保生效
                    Field f = eap$findFieldRecursive(menu1.getClass(), "page");
                    if (f != null) {
                        f.setAccessible(true);
                        f.set(menu1, newPage);
                    }
                } catch (Exception ignored) {}
                // 同步到本地 GUI 页码
                this.eap$currentPage = newPage;
                // 日志与强制重排（放在更新本地页码之后，确保布局读取到新页）
                eaep$LOGGER.info("[EAEP] NextPage clicked: {} -> {} (max={})", currentPage, newPage, maxPage);
                this.repositionSlots(SlotSemantics.ENCODED_PATTERN);
                this.repositionSlots(SlotSemantics.STORAGE);
                this.hoveredSlot = null;
                // 更新当前页可见状态
                eap$updatePageSlotActivity();
            }, Icon.ARROW_RIGHT);

            // 恢复到 AE2 左侧工具栏
            this.addToLeftToolbar(this.nextPage);
            this.addToLeftToolbar(this.prevPage);
        }

        // 倍增/倍减按钮：mixin在父类, 避免重复添加直接通过helper获取
    }

    @Override
    public int eap$getCurrentPage() {
        return getCurrentPage();
    }

    // 页码文本绘制移交给 AEBaseScreenMixin.renderLabels 尾部执行

    // 注意：不再注入 Screen#init，避免混入在某些映射情况下失败导致 TransformerError
    
    @Override
    public void eaep$updateButtonsStates() {
        // 只处理按钮可见性与定位，不再强制 showPage 或挪动 Slot 坐标，避免与原布局/tooltip 冲突
        if (nextPage != null && prevPage != null) {
            this.nextPage.setVisibility(true);
            this.prevPage.setVisibility(true);
        }

        if (this.eaep$scalingButtons.isEmpty())
            this.eaep$getButtons().forEach(button -> {
                var action = button.getAction();
                if (action == null) return;
                if (!"scaling".equals(action.getGroup())) return;
                if (!(button instanceof EAEPActionButton actionButton)) return;
                this.eaep$scalingButtons.add(actionButton);
            });

        this.eaep$lastScreenInfo = ButtonImplementations.updateScalingButtonsLayout(
                this,
                this.leftPos + this.imageWidth + 3,
                this.topPos + 68,
                this.eaep$lastScreenInfo
        );

        this.eaep$getButtons().forEach(button -> {
            if (!(button instanceof EAEPServerCycleButton cycleButton)) return;
            cycleButton.updateState();
        });

        // 每帧确保当前页槽位处于启用状态，非当前页禁用
        eap$updatePageSlotActivity();
    }

    // 本文件原包含本地样板缩放实现（单机模式）和 ExtendedAE 网络派发，已移除以兼容 1.21.1 与最小可构建集。
    

    @Unique
    private void eap$updatePageSlotActivity() {
        try {
            var list = this.getMenu().getSlots(SlotSemantics.ENCODED_PATTERN);
            if (list == null || list.isEmpty()) return;

            int currentPage = getCurrentPage();
            int base = currentPage * SLOTS_PER_PAGE;
            int end = Math.min(list.size(), base + SLOTS_PER_PAGE);

            for (int i = 0; i < list.size(); i++) {
                var slot = list.get(i);
                if (slot instanceof AppEngSlot s) {
                    boolean enabled = i >= base && i < end;
                    s.setActive(enabled);
                }
            }
        } catch (Throwable ignored) {}
    }

}