package com.extendedae_plus.util

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.init.EAEPItems
import com.fish.fishlib.util.keyBuilder.Patterns
import lombok.Getter
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

object UtilTextComponent {
    @JvmField
    val ModNameColorful: ComponentColorful = ComponentColorful(ExtendedAEPlus.MODNAME)

    @EventBusSubscriber(modid = ExtendedAEPlus.MODID)
    object RegistryColored {
        private val byKey = HashMap<String, ComponentColorful>()
        private val literal = HashMap<String, ComponentColorful>()

        @SubscribeEvent
        private fun onRegister(event: FMLLoadCompleteEvent) {
            listOf(
                EAEPItems.CellInfinity.asItem().descriptionId,
                UtilKeyBuilder.of(Patterns.Tooltip)
                    .item(EAEPItems.CellInfinity)
                    .addStr("description")
                    .addStr("colored")
                    .buildRaw()
            ).forEach(this::registerKey)
        }

        fun registerKey(key: String) {
            if (byKey.containsKey(key)) return
            this.registerKey(key, ComponentColorful(Component.translatable(key)))
        }

        fun registerKey(key: String, text: ComponentColorful) {
            if (byKey.containsKey(key)) return
            byKey[key] = text
        }

        @JvmStatic
        fun getOrCreate(original: Component): ComponentColorful {
            val key = when (val contents = original.contents) {
                is PlainTextContents -> contents.text()
                is TranslatableContents -> contents.key
                else -> original.string
            }

            var value = byKey[key] ?: literal[key]
            if (value == null) {
                value = ComponentColorful(original)
                literal[key] = value
            }
            return value
        }

        @JvmStatic
        fun find(key: String): Optional<ComponentColorful> =
            Optional.ofNullable(byKey[key] ?: literal[key])
    }

    class ComponentColorful @JvmOverloads constructor(
        private val componentOriginal: Component, frames: Int = 80
    ) : Component {
        private var original: String = ""

        var text = ArrayList<Component>()

        @Getter
        private var frames = 0
        private var initialized = false

        constructor(original: String) : this(Component.literal(original))

        init {
            this.frames = frames
            this.update(true)
        }

        fun update(refreshString: Boolean) {
            if (refreshString) {
                this.original = this.componentOriginal.string.replace("§[0-9a-zA-Z]".toRegex(), "")
                val contents = this.componentOriginal.contents
                if (contents is TranslatableContents) {
                    if (this.original != contents.key)
                        this.initialized = true
                } else this.initialized = true
            }
            this.text = IntStream.range(0, this.frames)
                .mapToObj(this::generatePart)
                .collect(Collectors.toList()) as ArrayList<Component>
        }

        fun get(): Component {
            if (!this.initialized) this.update(true)
            val state = Util.getMillis() / 70 % this.frames
            return text[state.toInt()]
        }

        fun setFrames(frames: Int) {
            this.frames = frames
            this.update(!this.initialized)
        }

        // ai
        // 反正我写不出来
        private fun generatePart(frame: Int): Component {
            val builder = Component.empty()
            val chars = this.original.toCharArray()
            val colorStretch = 0.03f

            // 动画偏移量：随着帧数增加，色相值增加，产生滚动感
            val animationOffset = frame.toFloat() / this.frames

            for (i in chars.indices) {
                val c = chars[i]
                if (c == ' ') {
                    builder.append(" ")
                    continue
                }

                // 计算该字符当前的 Hue (色相)
                // (i * colorStretch) 决定空间上的颜色分布
                // animationOffset 决定时间上的移动
                val hue = (animationOffset + (i * colorStretch)) % 1.0f

                // 将 HSB 转换为 RGB int
                // 饱和度 0.8f (略微柔和)，亮度 1.0f (明亮)
                val color = Mth.hsvToRgb(hue, 0.7f, 0.9f)
                builder.append(Component.literal(c.toString()).withColor(color))
            }
            return builder
        }

        override fun getStyle(): Style = this.get().style

        override fun getContents(): ComponentContents = this.get().contents

        override fun getSiblings(): MutableList<Component> = this.get().siblings

        override fun getVisualOrderText(): FormattedCharSequence = this.get().visualOrderText
    }

    class ClickEventCustomizable @JvmOverloads constructor(
        private val onClick: Runnable,
        private val callback: Component? = null
    ) : ClickEvent(
        Action.COPY_TO_CLIPBOARD, ""
    ) {
        fun trigger() {
            this.onClick.run()

            val player = Minecraft.getInstance().player
            if (player == null || this.callback == null) return
            player.displayClientMessage(this.callback, false)
        }

        override fun hashCode() = this.onClick.hashCode()

        override fun equals(other: Any?) = (other as? ClickEventCustomizable)?.hashCode() == this.hashCode()

        override fun toString(): String {
            return "ClickEventCustomizable{" +
                    "onClick=" + onClick +
                    '}'
        }
    }
}
