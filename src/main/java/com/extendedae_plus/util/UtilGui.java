package com.extendedae_plus.util;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.List;


/**
 * GUI工具类，提供样板获取、绘制等通用功能
 */
public final class UtilGui {
    /**
     * 从样板中获取输出数量文本
     *
     * @param pattern 样板物品
     * @return 格式化后的数量文本
     */
    public static String getPatternOutputText(ItemStack pattern) {
        if (pattern.isEmpty()) return "";

        var details = PatternDetailsHelper.decodePattern(pattern, Minecraft.getInstance().level);
        if (details == null) return "";

        List<GenericStack> outputs = details.getOutputs();
        if (outputs == null || outputs.isEmpty()) return "";

        GenericStack out = outputs.getFirst();
        long amount = out.amount();
        long perUnit = out.what().getAmountPerUnit();
        if (amount <= 0 || perUnit <= 0) return "";

        // 计算实际单位数量，支持小数
        double units = (double) amount / perUnit;
        if (units <= 0) return "";

        return formatNumberWithDecimal(units) + (perUnit > 1 ? "B" : "");
    }

    /**
     * 格式化带小数的数字，支持流体等需要显示小数的场景
     *
     * @param value 小数值
     * @return 格式化后的字符串
     */
    public static String formatNumberWithDecimal(double value) {
        if (value < 1000) {
            DecimalFormat smallDf = new DecimalFormat("#.##");
            // 小于1000时，若是整数则显示整数，否则显示最多两位小数
            if (value == (long) value) {
                return String.valueOf((long) value);
            } else {
                return smallDf.format(value);
            }
        }

        String[] preFixes = new String[]{"k", "M", "G", "T", "P", "E", "Z", "Y"};
        String level = "";
        for (int offset = 0; value >= 1000.0 && offset < preFixes.length; ++offset) {
            value /= 1000.0;
            level = preFixes[offset];
        }

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value) + level;
    }

    /**
     * 在槽位右下角绘制数量文本
     *
     * @param guiGraphics GUI图形上下文
     * @param font        字体
     * @param text        要绘制的文本
     * @param slotX       槽位X坐标
     * @param slotY       槽位Y坐标
     * @param scale       缩放比例
     */
    public static void drawAmountText(GuiGraphics guiGraphics, Font font, String text, int slotX, int slotY, float scale) {
        if (text.isEmpty()) {
            return;
        }

        // 计算缩放后的字体宽度，确保右对齐
        int scaledWidth = (int) (font.width(text) * scale);
        int textX = slotX + 16 - scaledWidth;
        int textY = slotY + 11; // 右下角显示

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300); // 提升 Z，确保在最上层
        guiGraphics.pose().scale(scale, scale, 1.0f); // 缩小字体
        guiGraphics.drawString(font, text, (int) (textX / scale), (int) (textY / scale), 0xFFFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    public static void renderScalableFakeItem(GuiGraphics guiGraphics,
                                              ItemStack itemStack,
                                              int x, int y, float scale) {
        if (itemStack.isEmpty()) return;

        var pose = guiGraphics.pose();
        var minecraft = Minecraft.getInstance();

        var bakedmodel = minecraft.getItemRenderer().getModel(itemStack, minecraft.level, null, 0);
        pose.pushPose();
        pose.translate(x + 8 * scale, y + 8 * scale, 150F);

        try {
            pose.scale(16.0F * scale, -16.0F * scale, 16.0F * scale);
            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) Lighting.setupForFlatItems();

            minecraft.getItemRenderer()
                    .render(itemStack,
                            ItemDisplayContext.GUI,
                            false,
                            pose,
                            guiGraphics.bufferSource(),
                            15728880,
                            OverlayTexture.NO_OVERLAY,
                            bakedmodel);
            guiGraphics.flush();
            if (flag) Lighting.setupFor3DItems();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
            crashreportcategory.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
            crashreportcategory.setDetail("Item Components", () -> String.valueOf(itemStack.getComponents()));
            crashreportcategory.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
            throw new ReportedException(crashreport);
        }

        guiGraphics.pose().popPose();
    }
} 