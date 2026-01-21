package com.extendedae_plus.util;

import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.stream.IntStream;

public class UtilTextComponent {
    public static class ModNameColorful {
        private static int frames;
        public static List<Component> text;

        static {
            regenerate(80);
        }

        public static Component get() {
            var state = Util.getMillis() / 70 % frames;
            return text.get((int) state);
        }

        public static void regenerate(int frames) {
            ModNameColorful.frames = frames;
            text = IntStream.range(0, frames)
                    .mapToObj(ModNameColorful::generatePart)
                    .toList();
        }

        // ai
        // 反正我写不出来
        private static Component generatePart(int frame) {
            var builder = Component.empty();
            var chars = ExtendedAEPlus.MODNAME.toCharArray();
            float colorStretch = 0.03f;

            // 动画偏移量：随着帧数增加，色相值增加，产生滚动感
            float animationOffset = (float) frame / frames;

            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == ' ') {
                    builder.append(" ");
                    continue;
                }

                // 计算该字符当前的 Hue (色相)
                // (i * colorStretch) 决定空间上的颜色分布
                // animationOffset 决定时间上的移动
                float hue = (animationOffset + (i * colorStretch)) % 1.0f;

                // 将 HSB 转换为 RGB int
                // 饱和度 0.8f (略微柔和)，亮度 1.0f (明亮)
                int color = Mth.hsvToRgb(hue, 0.7f, 0.9f);
                builder.append(Component.literal(String.valueOf(c)).withColor(color));
            }
            return builder;
        }
    }
}
