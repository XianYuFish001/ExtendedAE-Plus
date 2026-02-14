package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.TextOverride;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.Text;
import appeng.client.gui.style.TextAlignment;
import appeng.menu.AEBaseMenu;
import appeng.menu.slot.AppEngSlot;
import com.extendedae_plus.mixin.bridge.ExPatternPageAccessor;
import com.extendedae_plus.util.UtilGui;
import com.extendedae_plus.util.keyBuilder.Patterns;
import com.extendedae_plus.util.keyBuilder.UtilKeyBuilder;
import com.glodblock.github.extendedae.client.gui.GuiExPatternProvider;
import com.glodblock.github.extendedae.common.EAESingletons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO Refactor 惨不忍睹啊(
@Mixin(AEBaseScreen.class)
public abstract class MixinScreenBase<TMenu extends AEBaseMenu> extends AbstractContainerScreen<TMenu> {
    @Shadow
    @Final
    protected ScreenStyle style;

    public MixinScreenBase(TMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    /**
     * 重写renderSlot方法，为所有可见的样板槽位添加数量显示
     */
    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void eap$renderSlotAmounts(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (!(slot instanceof AppEngSlot appEngSlot)) return;
        if (!appEngSlot.isActive() || !appEngSlot.isSlotEnabled()) return;

        var itemStack = appEngSlot.getItem();
        if (itemStack.isEmpty()) return;

        var amountText = UtilGui.getPatternOutputText(itemStack);
        if (amountText.isEmpty()) return;
        UtilGui.drawAmountText(guiGraphics, this.font, amountText, appEngSlot.x, appEngSlot.y, 0.6f);
    }

    // 在 AEBaseScreen.drawText 完成某个文本绘制后，若该文本为“样板”标签，则紧接着绘制页码。
    @Inject(method = "drawText", at = @At("TAIL"))
    private void eap$appendPageAfterPatternsLabel(GuiGraphics guiGraphics,
                                                  Text text,
                                                  @Nullable TextOverride override,
                                                  CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof GuiExPatternProvider)) {
            return;
        }

        try {
            // 解析最终用于显示的标签内容
            Component content = text.getText();
            if (override != null && override.getContent() != null) {
                content = override.getContent().copy().withStyle(content.getStyle());
            }

            // 计算“样板”文本起点与宽度，按对齐方式与缩放修正 x/y
            Rect2i bounds = new Rect2i(0, 0, this.imageWidth, this.imageHeight);
            Point pos = text.getPosition().resolve(bounds);

            float scale = text.getScale();

            // 只关心第一行（标题类文本无换行或 maxWidth<=0）
            var contentLine = (text.getMaxWidth() <= 0)
                    ? content.getVisualOrderText()
                    : this.font.split(content, text.getMaxWidth()).getFirst();
            int lineWidth = this.font.width(contentLine);

            int x = pos.getX();
            int y = pos.getY();
            // 对齐修正
            var align = text.getAlign();
            if (align == TextAlignment.CENTER) {
                int textPx = Math.round(lineWidth * scale);
                x -= textPx / 2;
            } else if (align == TextAlignment.RIGHT) {
                int textPx = Math.round(lineWidth * scale);
                x -= textPx;
            }

            // 判断是否为“样板”组标题（多语言兼容且避免标题）
            boolean isPatterns = false;
            // 1) 基于翻译键
            var contents = content.getContents();
            if (contents instanceof TranslatableContents tc) {
                String key = tc.getKey();
                if (key != null && key.endsWith(".patterns")) {
                    isPatterns = true;
                }
            }
            // 2) 基于已知本地化键的字符串解析
            if (!isPatterns) {
                String label = content.getString();
                if (label != null) {
                    if (label.equals(Component.translatable("gui.pattern_provider.patterns").getString()))
                        isPatterns = true;
                    else if (label.equals(Component.translatable("gui.extendedae.patterns").getString()))
                        isPatterns = true;
                    else if (label.equals(Component.translatable("gui.ae2.patterns").getString())) isPatterns = true;
                }
            }
            // 3) 容错：中文“样板”且在标题下方（放宽到 y>=14）或文本正好等于“样板”
            if (!isPatterns) {
                String s = content.getString();
                if (s != null && ("样板".equals(s) || (s.contains("样板") && y >= 14))) {
                    isPatterns = true;
                }
            }
            if (!isPatterns) return;

            int cur = 1;
            int max = 1;
            if (self instanceof ExPatternPageAccessor accessor) {
                cur = Math.max(0, accessor.eap$getCurrentPage()) + 1;
            }
            try {
                var fMax = self.getClass().getDeclaredField("eap$maxPageLocal");
                fMax.setAccessible(true);
                Object v = fMax.get(self);
                if (v instanceof Integer i) {
                    max = Math.max(1, i);
                }
            } catch (Throwable ignored) {
            }

            String pageText = UtilKeyBuilder.of(Patterns.screen)
                    .item(EAESingletons.EX_PATTERN_PROVIDER.asItem())
                    .addStr("pages")
                    .args(cur, max)
                    .build().getString();

            int color = 0xFFFFFFFF;
            if (this.style != null) {
                try {
                    color = this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
                } catch (Throwable ignored) {
                }
            }
            int padding = 4;
            if (scale == 1.0f) {
                guiGraphics.drawString(font, pageText, x + lineWidth + padding, y, color, false);
            } else {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(x, y, 1);
                guiGraphics.pose().scale(scale, scale, 1);
                guiGraphics.drawString(font, pageText, lineWidth + padding, 0, color, false);
                guiGraphics.pose().popPose();
            }
        } catch (Throwable ignored) {
        }
    }
}
