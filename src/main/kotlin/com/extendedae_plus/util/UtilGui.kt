package com.extendedae_plus.util

import appeng.api.crafting.PatternDetailsHelper
import com.mojang.blaze3d.platform.Lighting
import net.minecraft.CrashReport
import net.minecraft.ReportedException
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import java.text.DecimalFormat

/**
 * GUI工具类，提供样板获取、绘制等通用功能
 */
object UtilGui {
    /**
     * 从样板中获取输出数量文本
     * 
     * @param pattern 样板物品
     * @return 格式化后的数量文本
     */
    @JvmStatic
    fun getPatternOutputText(pattern: ItemStack): String {
        if (pattern.isEmpty) return ""

        val details = PatternDetailsHelper.decodePattern(pattern, Minecraft.getInstance().level)
            ?: return ""

        val outputs = details.outputs
        if (outputs == null || outputs.isEmpty()) return ""

        val out = outputs[0]
        val amount = out.amount()
        val perUnit = out.what().amountPerUnit.toLong()
        if (amount <= 0 || perUnit <= 0) return ""

        // 计算实际单位数量，支持小数
        val units = amount.toDouble() / perUnit
        if (units <= 0) return ""

        return formatNumberWithDecimal(units) + (if (perUnit > 1) "B" else "")
    }

    /**
     * 格式化带小数的数字，支持流体等需要显示小数的场景
     * 
     * @param value 小数值
     * @return 格式化后的字符串
     */
    fun formatNumberWithDecimal(value: Double): String {
        var value = value
        if (value < 1000) {
            val smallDf = DecimalFormat("#.##")
            // 小于1000时，若是整数则显示整数，否则显示最多两位小数
            return if (value == value.toLong().toDouble()) {
                value.toLong().toString()
            } else {
                smallDf.format(value)
            }
        }

        val preFixes = arrayOf("k", "M", "G", "T", "P", "E", "Z", "Y")
        var level = ""
        var offset = 0
        while (value >= 1000.0 && offset < preFixes.size) {
            value /= 1000.0
            level = preFixes[offset]
            ++offset
        }

        val df = DecimalFormat("#.##")
        return df.format(value) + level
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
    @JvmStatic
    fun drawAmountText(guiGraphics: GuiGraphics, font: Font, text: String, slotX: Int, slotY: Int, scale: Float) {
        if (text.isEmpty()) {
            return
        }

        // 计算缩放后的字体宽度，确保右对齐
        val scaledWidth = (font.width(text) * scale).toInt()
        val textX = slotX + 16 - scaledWidth
        val textY = slotY + 11 // 右下角显示

        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(0f, 0f, 300f) // 提升 Z，确保在最上层
        guiGraphics.pose().scale(scale, scale, 1.0f) // 缩小字体
        guiGraphics.drawString(font, text, (textX / scale).toInt(), (textY / scale).toInt(), -0x1, true)
        guiGraphics.pose().popPose()
    }

    @JvmStatic
    fun GuiGraphics.renderFakeItemScalable(
        itemStack: ItemStack,
        x: Int,
        y: Int, scale: Float
    ) {
        if (itemStack.isEmpty) return

        val pose = pose()
        val minecraft = Minecraft.getInstance()

        val bakedmodel = minecraft.itemRenderer.getModel(itemStack, minecraft.level, null, 0)
        pose.pushPose()
        pose.translate(x + 8 * scale, y + 8 * scale, 150f)

        try {
            pose.scale(16.0f * scale, -16.0f * scale, 16.0f * scale)
            val flag = !bakedmodel.usesBlockLight()
            if (flag) Lighting.setupForFlatItems()

            minecraft.itemRenderer
                .render(
                    itemStack,
                    ItemDisplayContext.GUI,
                    false,
                    pose,
                    bufferSource(),
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    bakedmodel
                )
            flush()
            if (flag) Lighting.setupFor3DItems()
        } catch (throwable: Throwable) {
            val crashreport = CrashReport.forThrowable(throwable, "Rendering item")
            val crashreportcategory = crashreport.addCategory("Item being rendered")
            crashreportcategory.setDetail("Item Type") { itemStack.item.toString() }
            crashreportcategory.setDetail("Item Components") { itemStack.getComponents().toString() }
            crashreportcategory.setDetail("Item Foil") { itemStack.hasFoil().toString() }
            throw ReportedException(crashreport)
        }

        pose().popPose()
    }
}