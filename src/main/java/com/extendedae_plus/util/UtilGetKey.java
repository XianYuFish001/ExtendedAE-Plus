package com.extendedae_plus.util;

import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * <h3>工具类GetKey: 提供更便捷的翻译键生成与使用功能
 * <h6> 运行时调用:
 * <p> 通过非dataOnly的方法传入组成translationKey的参数, 可以build为Component或直接获取原字符串
 * <h6> DataGen时调用:
 * <p> 需要提前使用{@link #bindTranslator(String, BiConsumer)}绑定该provider的locale与add方法,
 * <br/> 可以实现快速生成并add, 跳过繁杂调用嵌套
 * <br/> 调用完成后需要手动执行 {@link #destroy(String)}
 * <p> dataOnly方法:
 * {@link #buildInto(String, String)} {@link #branch(String, String, String)}
 */
public class UtilGetKey {
    private String keyTemplate = "%s%s";
    private String mainDescription = ExtendedAEPlus.MODID;
    private String additionalKey = "";
    private Object[] args;

    // Data-Only Fields
    private static final boolean checkedEnv = "data".equals(System.getProperty("extendedae_plus.environment"));
    private static final Map<String, BiConsumer<String, String>> translators = new HashMap<>();
    private static final ThreadLocal<String> selectedLocale = ThreadLocal.withInitial(() -> "en_us");

    public static final String creativeTab = "creative_tab.%s%s";
    public static final String tooltip = "tooltip.%s%s";
    public static final String screenTooltip = "tooltip.screen.%s%s";
    public static final String message = "message.%s%s";
    public static final String actionBar = "message.actionbar.%s%s";
    public static final String screen = "screen.%s%s";
    public static final String keywordGroup = "keywordGroup.%s%s";
    public static final String config = "%s.configuration%s";
    public static final String key = "key.%s%s";
    public static final String keyCategory = "key.category.%s%s";
    public static final String viewerInfo = "recipe_viewer.info.%s%s";
    public static final String viewerTooltip = "recipe_viewer.tooltip.%s%s";
    public static final String viewerCategory = "recipe_viewer.category.%s%s";
    public static final String jadeInfo = "jade.info.%s%s";
    public static final String jadeConfig = "config.jade.plugin_%s%s";

    public UtilGetKey(UtilGetKey original) {
        this.keyTemplate = original.keyTemplate;
        this.mainDescription = original.mainDescription;
        this.additionalKey = original.additionalKey;
        this.args = original.args;
    }

    public UtilGetKey(String keyTemplate) {
        this.keyTemplate = keyTemplate;
    }

    public UtilGetKey(DeferredItem<?> item) {
        this.mainDescription = item.asItem().getDescriptionId().toLowerCase();
    }

    public UtilGetKey(DeferredHolder<FluidType, ?> fluid) {
        this.mainDescription = fluid.get().getDescriptionId().toLowerCase();
    }

    public UtilGetKey type(String template) {
        this.keyTemplate = template;
        return this;
    }

    public UtilGetKey item(ItemStack item) {
        this.mainDescription = item.getDescriptionId().toLowerCase();
        return this;
    }

    public UtilGetKey item(Item item) {
        this.mainDescription = item.getDescriptionId().toLowerCase();
        return this;
    }

    public UtilGetKey item(DeferredItem<?> item) {
        this.mainDescription = item.asItem().getDescriptionId().toLowerCase();
        return this;
    }

    public UtilGetKey addStr(String additionalKey) {
        if (!this.additionalKey.endsWith("."))
            this.additionalKey += ".";
        this.additionalKey += additionalKey;
        return this;
    }

    public UtilGetKey addStr(boolean condition, String additionalKey) {
        if (condition) {
            this.addStr(additionalKey);
        }
        return this;
    }

    public UtilGetKey addStr(boolean condition, String keyA, String keyB) {
        return this.addStr(condition ? keyA : keyB);
    }

    public UtilGetKey args(Object... args) {
        this.args = Arrays.stream(args)
                .map(object -> {
                    if (object == null) return "";
                    else if (!TranslatableContents.isAllowedPrimitiveArgument(object))
                        return object.toString();
                    else return object;
                }).toArray();
        return this;
    }

    public UtilGetKey args(boolean condition, Object... args) {
        if (condition) this.args(args);
        return this;
    }

    public String buildRaw() {
        var currentKey = String.format(this.keyTemplate, this.mainDescription, this.additionalKey);
        // For test
        HelperI18nKeySaver.recordKey(currentKey);

        return currentKey;
    }

    public MutableComponent build() {
        var currentKey = this.buildRaw();

        if (this.args == null) return Component.translatable(currentKey);
        else return Component.translatable(currentKey, this.args);
    }

    // Data-Only Methods
    public UtilGetKey branch(String additionalKey, String value, String locale) {
        new UtilGetKey(this)
                .addStr(additionalKey)
                .buildInto(value, locale);
        return this;
    }

    public UtilGetKey branch(String additionalKey, String value) {
        return this.branch(additionalKey, value, selectedLocale.get());
    }

    public void buildInto(String value, String locale) {
        checkEnvironment();
        translators.getOrDefault(locale, (k, v) -> {}).accept(this.buildRaw(), value);
    }

    public void buildInto(String value) {
        this.buildInto(value, selectedLocale.get());
    }

    public static void bindTranslator(String locale, BiConsumer<String, String> translator) {
        checkEnvironment();
        translators.put(locale, translator);
        selectedLocale.set(locale);
    }

    public static void destroy(String locale) {
        checkEnvironment();
        translators.remove(locale);
        selectedLocale.remove();
    }

    public static void checkEnvironment() {
        if (!checkedEnv) throw new IllegalStateException("Cannot use data-only methods outside of the runData phase");
    }
}
