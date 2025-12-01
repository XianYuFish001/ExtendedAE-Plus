package com.extendedae_plus.mixin.core.extendedae.client.gui;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import appeng.menu.AEBaseMenu;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.network.CPacketUploadInventoryPattern;
import com.extendedae_plus.util.GuiUtil;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Pseudo
@Mixin(value = GuiExPatternTerminal.class, remap = false)
public abstract class GuiExPatternTerminalMixin extends AEBaseScreen<AEBaseMenu> {
    @Unique
    private IconButton eap$toggleSlotsButton;
    @Unique
    private boolean eap$showSlots = false; // 默认由配置初始化
    @Unique
    private long eap$currentlyChoicePatterProvider = -1; // 当前选择的样板供应器ID
    @Unique
    private final Map<Integer, Button> eap$openUIButtons = new HashMap<>();

    public GuiExPatternTerminalMixin(AEBaseMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    /**
     * 设置当前选择的样板供应器ID
     */
    @Unique
    public void setCurrentlyChoicePatternProvider(long id) {
        this.eap$currentlyChoicePatterProvider = id;
    }

    /**
     * 拦截鼠标点击事件，实现Shift+左键快速上传样板功能
     * 注意：某些整合包的 ExtendedAE 版本不在该类中覆写 mouseClicked，此处设置 require=0 以防止注入失败导致崩溃。
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // 古埃及奇观: 金字塔
        // 检查是否是左键点击 + Shift键
        if (button == 0 && hasShiftDown()) {
            // 获取点击的槽位
            Slot hoveredSlot = this.getSlotUnderMouse();
            if (hoveredSlot != null && hoveredSlot.container == this.minecraft.player.getInventory()) {
                // 点击的是玩家背包槽位
                ItemStack clickedItem = hoveredSlot.getItem();

                // 检查是否是有效的编码样板
                if (!clickedItem.isEmpty() && PatternDetailsHelper.isEncodedPattern(clickedItem)) {
                    // 检查是否选择了样板供应器
                    if (eap$currentlyChoicePatterProvider != -1) {
                        // 执行快速上传
                        this.eap$quickUploadPattern(hoveredSlot.getSlotIndex());

                        // 取消默认的点击行为
                        cir.setReturnValue(true);
                    } else {
                        // 显示提示消息：请先选择一个样板供应器
                        if (this.minecraft.player != null) {
                            this.minecraft.player.displayClientMessage(
                                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                                            .addStr("provider_to_upload")
                                            .addStr("unset")
                                            .build(),
                                    true
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * 快速上传样板到当前选择的供应器
     */
    @Unique
    private void eap$quickUploadPattern(int playerSlotIndex) {
        if (this.minecraft.player != null) {
            // 获取要上传的物品
            ItemStack itemToUpload = this.minecraft.player.getInventory().getItem(playerSlotIndex);

            if (!itemToUpload.isEmpty() && PatternDetailsHelper.isEncodedPattern(itemToUpload)) {
                // 改用我们自己的网络包，直接将玩家槽位与选择的供应器ID发送到服务器
                try {
                    PacketDistributor.sendToServer(new CPacketUploadInventoryPattern(
                            playerSlotIndex,
                            eap$currentlyChoicePatterProvider
                    ));
                } catch (Throwable t) {
                    // 理论上不会失败，若失败则给出简要提示
                    this.minecraft.player.displayClientMessage(
                            UtilKeyBuilder.of(UtilKeyBuilder.message)
                                    .addStr("provider_to_upload")
                                    .addStr("failed")
                                    .build(),
                            false
                    );
                }
            } else {
                this.minecraft.player.displayClientMessage(
                        UtilKeyBuilder.of(UtilKeyBuilder.message)
                                .addStr("provider_to_upload")
                                .addStr("invalid_pattern")
                                .build(),
                        false
                );
            }
        }
    }

    @Inject(method = "<init>(Lcom/glodblock/github/extendedae/container/ContainerExPatternTerminal;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;Lappeng/client/gui/style/ScreenStyle;)V", at = @At("TAIL"), remap = false, require = 0)
    private void injectConstructor(com.glodblock.github.extendedae.container.ContainerExPatternTerminal menu,
                                   Inventory playerInventory,
                                   Component title,
                                   ScreenStyle style,
                                   CallbackInfo ci) {
        // 根据配置初始化默认显示/隐藏状态
        try {
            this.eap$showSlots = EAEPConfig.PATTERN_TERMINAL_SHOW_SLOTS_DEFAULT.get();
        } catch (Throwable ignored) {
        }
        // 创建切换槽位显示的按钮
        this.eap$toggleSlotsButton = new IconButton((b) -> {
            this.eap$showSlots = !this.eap$showSlots; // 开关状态

            // 通过反射调用refreshList方法 - 先尝试当前类，失败后尝试父类
            try {
                Method refreshMethod = null;
                try {
                    // 先尝试在当前类中查找
                    refreshMethod = this.getClass().getDeclaredMethod("refreshList");
                } catch (NoSuchMethodException e1) {
                    // 如果当前类没有，尝试在父类中查找
                    refreshMethod = this.getClass().getSuperclass().getDeclaredMethod("refreshList");
                }

                refreshMethod.setAccessible(true);
                refreshMethod.invoke(this);
            } catch (Exception ignored) {
            }
        }) {
            @Override
            protected Icon getIcon() {
                return eap$showSlots ? Icon.PATTERN_ACCESS_HIDE : Icon.PATTERN_ACCESS_SHOW;
            }
        };

        // 设置按钮提示文本
        this.eap$toggleSlotsButton.setTooltip(Tooltip.create(UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
                .addStr("toggle_slots_display")
                .build()));

        // 添加到左侧工具栏
        this.addToLeftToolbar(this.eap$toggleSlotsButton);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false, require = 0)
    private void eap$onInit(CallbackInfo ci) {
        // 清理旧的打开UI按钮
        this.eap$openUIButtons.values().forEach(this::removeWidget);
        this.eap$openUIButtons.clear();
    }

    @Inject(method = "refreshList", at = @At("HEAD"), remap = false, require = 0)
    private void onRefreshListStart(CallbackInfo ci) {
        // 更新按钮图标
        if (this.eap$toggleSlotsButton != null) {
            this.eap$toggleSlotsButton.setTooltip(Tooltip.create(UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
                    .addStr("toggle_slot_display")
                    .addStr(this.eap$showSlots, "enabled", "disabled")
                    .build()));
        }
        // 清理旧的打开UI按钮
        this.eap$openUIButtons.values().forEach(this::removeWidget);
        this.eap$openUIButtons.clear();
    }

    @Inject(method = "refreshList", at = @At("TAIL"), remap = false, require = 0)
    private void onRefreshListEnd(CallbackInfo ci) {

        // 在refreshList结束后，根据showSlots状态过滤SlotsRow
        if (!this.eap$showSlots) {
            try {
                // 通过反射访问rows字段 - 先尝试当前类，失败后尝试父类
                java.lang.reflect.Field rowsField = null;
                try {
                    // 先尝试在当前类中查找
                    rowsField = this.getClass().getDeclaredField("rows");
                } catch (NoSuchFieldException e1) {
                    // 如果当前类没有，尝试在父类中查找
                    rowsField = this.getClass().getSuperclass().getDeclaredField("rows");
                }
                rowsField.setAccessible(true);
                java.util.ArrayList<?> rows = (java.util.ArrayList<?>) rowsField.get(this);

                // 通过反射访问highlightBtns字段
                java.lang.reflect.Field highlightBtnsField = null;
                try {
                    // 先尝试在当前类中查找
                    highlightBtnsField = this.getClass().getDeclaredField("highlightBtns");
                } catch (NoSuchFieldException e1) {
                    // 如果当前类没有，尝试在父类中查找
                    highlightBtnsField = this.getClass().getSuperclass().getDeclaredField("highlightBtns");
                }
                highlightBtnsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.HashMap<Integer, Object> highlightBtns = (java.util.HashMap<Integer, Object>) highlightBtnsField.get(this);

                // 创建新的索引映射
                java.util.HashMap<Integer, Object> newHighlightBtns = new java.util.HashMap<>();
                int newIndex = 0;

                // 移除所有SlotsRow，只保留GroupHeaderRow，同时重新映射高亮按钮索引
                for (int i = 0; i < rows.size(); i++) {
                    Object row = rows.get(i);
                    String className = row.getClass().getSimpleName();

                    if (className.equals("GroupHeaderRow")) {
                        // 保留GroupHeaderRow，并重新映射对应的高亮按钮
                        @SuppressWarnings("unchecked")
                        java.util.ArrayList<Object> typedRows = (java.util.ArrayList<Object>) rows;
                        typedRows.set(newIndex, row);

                        // 查找原来在这个位置的高亮按钮
                        // 原始代码中，高亮按钮的索引是在添加GroupHeaderRow之后、添加第一个SlotsRow之前设置的
                        // 所以按钮的索引指向的是第一个SlotsRow的位置
                        // 我们需要查找索引为 i+1 的按钮（第一个SlotsRow的位置）
                        if (highlightBtns.containsKey(i + 1)) {
                            Object button = highlightBtns.get(i + 1);
                            newHighlightBtns.put(newIndex, button);
                        }

                        newIndex++;
                    }
                }

                // 移除多余的行
                while (rows.size() > newIndex) {
                    rows.removeLast();
                }

                // 更新highlightBtns
                highlightBtns.clear();
                highlightBtns.putAll(newHighlightBtns);

                // 强制刷新滚动条
                try {
                    Method resetScrollbarMethod = null;
                    try {
                        // 先尝试在当前类中查找
                        resetScrollbarMethod = this.getClass().getDeclaredMethod("resetScrollbar");
                    } catch (NoSuchMethodException e1) {
                        // 如果当前类没有，尝试在父类中查找
                        resetScrollbarMethod = this.getClass().getSuperclass().getDeclaredMethod("resetScrollbar");
                    }

                    resetScrollbarMethod.setAccessible(true);
                    resetScrollbarMethod.invoke(this);
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Inject(method = "drawFG", at = @At("TAIL"), remap = false, require = 0)
    private void eap$afterDrawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, CallbackInfo ci) {
        // 原有的搜索高亮逻辑（使用反射以兼容不同版本的 ExtendedAE GUI）
        try {
            // 仅当任一搜索框非空时绘制叠加层
            boolean searchActive = false;
            try {
                var fOut = this.getClass().getDeclaredField("searchOutField");
                fOut.setAccessible(true);
                Object outField = fOut.get(this);
                if (outField != null) {
                    var mGetVal = outField.getClass().getMethod("getValue");
                    Object val = mGetVal.invoke(outField);
                    if (val instanceof String s && !s.isEmpty()) {
                        searchActive = true;
                    }
                }
            } catch (NoSuchFieldException ignored) {
            }
            if (!searchActive) {
                try {
                    var fIn = this.getClass().getDeclaredField("searchInField");
                    fIn.setAccessible(true);
                    Object inField = fIn.get(this);
                    if (inField != null) {
                        var mGetVal = inField.getClass().getMethod("getValue");
                        Object val = mGetVal.invoke(inField);
                        if (val instanceof String s && !s.isEmpty()) {
                            searchActive = true;
                        }
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }

            if (!searchActive) {
                return;
            }

            // 读取 matchedStack 与 matchedProvider
            Set<ItemStack> matchedStack = null;
            Set<PatternContainerRecord> matchedProvider = null;
            try {
                var fMs = this.getClass().getDeclaredField("matchedStack");
                fMs.setAccessible(true);
                Object ms = fMs.get(this);
                if (ms instanceof Set<?> s) {
                    // 原始是 Set<ItemStack>
                    @SuppressWarnings("unchecked")
                    Set<ItemStack> cast = (Set<ItemStack>) s;
                    matchedStack = cast;
                }
            } catch (NoSuchFieldException ignored) {
            }
            try {
                var fMp = this.getClass().getDeclaredField("matchedProvider");
                fMp.setAccessible(true);
                Object mp = fMp.get(this);
                if (mp instanceof Set<?> s) {
                    @SuppressWarnings("unchecked")
                    Set<PatternContainerRecord> cast = (Set<PatternContainerRecord>) s;
                    matchedProvider = cast;
                }
            } catch (NoSuchFieldException ignored) {
            }

            if (matchedStack == null || matchedProvider == null) {
                return; // 缺少必要数据则不绘制
            }

            // 使用 GuiUtil 的通用绘制方法绘制槽位高亮（包含彩虹流转效果）
            GuiUtil.drawPatternSlotHighlights(guiGraphics, this.menu.slots, matchedStack, matchedProvider);
        } catch (Throwable ignored) {}
    }
}