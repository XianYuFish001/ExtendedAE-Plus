package com.extendedae_plus.util;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModItems;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

public class UtilTextComponent {
    public static final ComponentColorful modNameColorful = new ComponentColorful(ExtendedAEPlus.MODNAME);

    @EventBusSubscriber(modid = ExtendedAEPlus.MODID)
    public static class RegistryColored {
        private static final Map<String, ComponentColorful> byKey = new HashMap<>();
        private static final Map<String, ComponentColorful> literal = new HashMap<>();

        @SubscribeEvent
        private static void onRegister(FMLLoadCompleteEvent event) {
            List.of(
                    ModItems.INFINITY_BIGINTEGER_CELL_ITEM.asItem().getDescriptionId(),
                    UtilKeyBuilder.of(UtilKeyBuilder.tooltip).item(ModItems.INFINITY_BIGINTEGER_CELL_ITEM).addStr("description").addStr("colored").buildRaw()
            ).forEach(RegistryColored::registerKey);
        }

        public static void registerKey(String key) {
            if (byKey.containsKey(key)) return;
            registerKey(key, new ComponentColorful(Component.translatable(key)));
        }

        public static void registerKey(String key, ComponentColorful text) {
            if (byKey.containsKey(key)) return;
            byKey.put(key, text);
        }

        public static ComponentColorful getOrCreate(Component original) {
            var key = switch (original.getContents()) {
                case PlainTextContents contents -> contents.text();
                case TranslatableContents contents -> contents.getKey();
                default -> original.getString();
            };

            var value = byKey.get(key);
            if (value == null) value = literal.get(key);
            if (value == null) {
                value = new ComponentColorful(original);
                literal.put(key, value);
            }
            return value;
        }

        public static Optional<ComponentColorful> find(String key) {
            return Optional.ofNullable(byKey.get(key)).or(() -> Optional.ofNullable(literal.get(key)));
        }
    }

    public static class ComponentColorful implements Component {
        private final Component componentOriginal;
        private String original;

        public List<Component> text = new ArrayList<>();
        private int frames;
        private boolean initialized;

        public ComponentColorful(Component original) {
            this(original, 80);
        }

        public ComponentColorful(String original) {
            this(Component.literal(original));
        }

        public ComponentColorful(Component original, int frames) {
            this.componentOriginal = original;
            this.frames = frames;
            this.update(true);
        }

        public void update(boolean refreshString) {
            if (refreshString) {
                this.original = this.componentOriginal.getString().replaceAll("§[0-9a-zA-Z]", "");
                if (this.componentOriginal.getContents() instanceof TranslatableContents contents) {
                    if (!this.original.equals(contents.getKey()))
                        this.initialized = true;
                } else this.initialized = true;
            }
            this.text = IntStream.range(0, this.frames)
                    .mapToObj(this::generatePart)
                    .toList();
        }

        public Component get() {
            if (!this.initialized)
                this.update(true);
            var state = Util.getMillis() / 70 % this.frames;
            return text.get((int) state);
        }

        public int getFrames() {
            return this.frames;
        }

        public void setFrames(int frames) {
            this.frames = frames;
            this.update(!this.initialized);
        }

        // ai
        // 反正我写不出来
        private Component generatePart(int frame) {
            var builder = Component.empty();
            var chars = this.original.toCharArray();
            float colorStretch = 0.03f;

            // 动画偏移量：随着帧数增加，色相值增加，产生滚动感
            float animationOffset = (float) frame / this.frames;

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

        @Override
        public Style getStyle() {
            return this.get().getStyle();
        }

        @Override
        public ComponentContents getContents() {
            return this.get().getContents();
        }

        @Override
        public List<Component> getSiblings() {
            return this.get().getSiblings();
        }

        @Override
        public FormattedCharSequence getVisualOrderText() {
            return this.get().getVisualOrderText();
        }
    }

    public static class ClickEventCustomizable extends ClickEvent {
        private final Runnable onClick;
        private final @Nullable Component callback;

        public ClickEventCustomizable(Runnable onClick) {
            this(onClick, null);
        }

        public ClickEventCustomizable(Runnable onClick, @Nullable Component callback) {
            super(Action.COPY_TO_CLIPBOARD, "");
            this.onClick = onClick;
            this.callback = callback;
        }

        public void trigger() {
            this.onClick.run();

            var player = Minecraft.getInstance().player;
            if (player == null || this.callback == null) return;
            player.displayClientMessage(this.callback, false);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.onClick);
        }

        @Override
        public String toString() {
            return "ClickEventCustomizable{" +
                    "onClick=" + onClick +
                    '}';
        }
    }
}
