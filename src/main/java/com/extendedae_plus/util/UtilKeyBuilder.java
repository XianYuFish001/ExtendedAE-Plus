package com.extendedae_plus.util;

import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * <h2> 工具类KeyBuilder: 再次提供更便捷的翻译键生成与使用功能
 * <h4> {@link BuilderGeneric}
 * <p> 没什么好解释的, 一看就懂
 * <h4> {@link BuilderDataGen}
 * <p> 以前的DataGenOnly方法被写成了一个独立的类
 * <p> 具体的用法还是保持不变, 在使用前后需要调用
 * <p> {@linkplain  BuilderDataGen#bindTranslator(String, BiConsumer)}
 * <p> {@linkplain  BuilderDataGen#destroy(String)}
 * <p> 具体添加了快速生成并帮助add的辅助方法
 * <h4> {@link BuilderSnapshotable}
 * <p> 添加了可以使状态快速回溯的辅助方法
 * <h4> {@link BuilderCollection} {@link BuilderMap}
 * <p> 借助状态回溯机制实现了与以前(和现在)dataGen方法的便利添加逻辑, 甚至更加好用
 * <p> 灵感来源于{@link ModConfigSpec}, 虽然实际上很不同
 */
public class UtilKeyBuilder {
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

    // 高雅人士正在再次编写工具类.jpg

    public static BuilderGeneric<?> of(String keyTemplate) {
        var builder = new BuilderGeneric<>();
        builder.keyTemplate = keyTemplate;
        return builder;
    }

    public static BuilderGeneric<?> of(DeferredHolder<?, ?> holder) {
        var builder = new BuilderGeneric<>();
        builder.mainDescription = switch (holder.get()) {
            case Item item -> item.getDescriptionId().toLowerCase();
            case FluidType fluidType -> fluidType.getDescriptionId().toLowerCase();
            default -> ExtendedAEPlus.MODID;
        };
        return builder;
    }

    public static BuilderDataGen ofDataGen(String keyTemplate) {
        BuilderDataGen.checkEnvironment();
        var builder = new BuilderDataGen();
        builder.keyTemplate = keyTemplate;
        return builder;
    }

    public static BuilderDataGen ofDataGen(DeferredHolder<?, ?> holder) {
        BuilderDataGen.checkEnvironment();
        var builder = new BuilderDataGen();
        builder.mainDescription = switch (holder.get()) {
            case Item item -> item.getDescriptionId().toLowerCase();
            case FluidType fluidType -> fluidType.getDescriptionId().toLowerCase();
            default -> ExtendedAEPlus.MODID;
        };
        return builder;
    }

    public static class BuilderGeneric<TBuilder extends BuilderGeneric<TBuilder>> {
        protected String keyTemplate = "%s%s";
        protected String mainDescription = ExtendedAEPlus.MODID;
        protected String additionalKey = "";
        protected Object[] args;

        private BuilderGeneric() {
        }

        protected BuilderGeneric(BuilderGeneric<TBuilder> original) {
            this.copyFrom(original);
        }

        @SuppressWarnings("unchecked")
        private TBuilder self() {
            return (TBuilder) this;
        }

        public BuilderCollection bindCollection(Collection<Component> target) {
            return new BuilderCollection((BuilderCollection) this, target);
        }

        public BuilderCollection newArrayList() {
            return this.bindCollection(new ArrayList<>());
        }

        @SuppressWarnings("unchecked")
        public <TKey> BuilderMap<TKey> bindMap(Map<TKey, Component> target) {
            return new BuilderMap<>((BuilderGeneric<BuilderMap<TKey>>) this, target);
        }

        public BuilderMap<String> newHashMap() {
            return this.bindMap(new HashMap<>());
        }

        protected void copyFrom(BuilderGeneric<TBuilder> original) {
            this.keyTemplate = original.keyTemplate;
            this.mainDescription = original.mainDescription;
            this.additionalKey = original.additionalKey;
            this.args = original.args;
        }

        public TBuilder type(String keyTemplate) {
            this.keyTemplate = keyTemplate;
            return this.self();
        }

        public TBuilder item(ItemLike item) {
            this.mainDescription = item.asItem().getDescriptionId().toLowerCase();
            return this.self();
        }

        public TBuilder item(ItemStack itemStack) {
            return this.item(itemStack.getItem());
        }

        public TBuilder addStr(String additionalKey) {
            if (!this.additionalKey.endsWith("."))
                this.additionalKey += ".";
            this.additionalKey += additionalKey;
            return this.self();
        }

        public TBuilder addStr(boolean condition, String additionalKey) {
            if (condition) this.addStr(additionalKey);
            return this.self();
        }

        public TBuilder addStr(boolean condition, String keyA, String keyB) {
            return this.addStr(condition ? keyA : keyB);
        }

        public TBuilder args(Object... args) {
            this.args = Arrays.stream(args)
                    .map(object -> {
                        if (object == null) return "";
                        else if (!TranslatableContents.isAllowedPrimitiveArgument(object))
                            return object.toString();
                        else return object;
                    }).toArray();
            return this.self();
        }

        public TBuilder args(boolean condition, Object... args) {
            if (condition) this.args(args);
            return this.self();
        }

        public String buildRaw() {
            // For test
            // HelperI18nKeySaver.recordKey(currentKey);
            return String.format(this.keyTemplate, this.mainDescription, this.additionalKey);
        }

        public MutableComponent build() {
            var currentKey = this.buildRaw();

            if (this.args == null) return Component.translatable(currentKey);
            else return Component.translatable(currentKey, this.args);
        }
    }

    public static class BuilderDataGen extends BuilderGeneric<BuilderDataGen> {
        private static final Map<String, BiConsumer<String, String>> translators = new HashMap<>();
        private static final ThreadLocal<String> selectedLocale = ThreadLocal.withInitial(() -> "en_us");

        private BuilderDataGen() {
        }

        protected BuilderDataGen(BuilderDataGen original) {
            super(original);
        }

        public BuilderDataGen branch(String additionalKey, String value, String locale) {
            new BuilderDataGen(this)
                    .addStr(additionalKey)
                    .buildInto(value, locale);
            return this;
        }

        public BuilderDataGen branch(String additionalKey, String value) {
            return this.branch(additionalKey, value, selectedLocale.get());
        }

        public void buildInto(String value, String locale) {
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
            if (!DatagenModLoader.isRunningDataGen())
                throw new IllegalStateException("Cannot use data-only methods outside of the runData phase");
        }
    }

    public abstract static class BuilderSnapshotable<TBuilder extends BuilderSnapshotable<TBuilder>>
            extends BuilderGeneric<TBuilder> {
        protected TBuilder snapshot = null;

        protected BuilderSnapshotable(BuilderGeneric<TBuilder> original, boolean saveSnapshot) {
            super(original);
            if (saveSnapshot) this.saveSnapshot();
        }

        public abstract TBuilder saveSnapshot();

        protected void restoreSnapshot() {
            if (this.snapshot == null) return;
            this.copyFrom(this.snapshot);
        }
    }

    public static class BuilderCollection extends BuilderSnapshotable<BuilderCollection> {
        private final Collection<Component> target;

        protected BuilderCollection(BuilderGeneric<BuilderCollection> original, Collection<Component> target) {
            this(original, target, true);
        }

        protected BuilderCollection(BuilderGeneric<BuilderCollection> original,
                                    Collection<Component> target,
                                    boolean saveSnapshot) {
            super(original, saveSnapshot);
            this.target = target;
        }

        @Override
        public BuilderCollection saveSnapshot() {
            this.snapshot = new BuilderCollection(this, this.target, false);
            return this;
        }

        public BuilderCollection buildInto() {
            this.target.add(this.build());
            this.restoreSnapshot();
            return this;
        }

        public BuilderCollection buildInto(String additionalKey) {
            this.target.add(this.addStr(additionalKey).build());
            this.restoreSnapshot();
            return this;
        }

        public Collection<Component> getCollection() {
            return this.target;
        }
    }

    public static class BuilderMap<TKey> extends BuilderSnapshotable<BuilderMap<TKey>> {
        private final Map<TKey, Component> target;

        protected BuilderMap(BuilderGeneric<BuilderMap<TKey>> original, Map<TKey, Component> target) {
            this(original, target, true);
        }

        protected BuilderMap(BuilderGeneric<BuilderMap<TKey>> original,
                             Map<TKey, Component> target,
                             boolean saveSnapshot) {
            super(original, saveSnapshot);
            this.target = target;
        }

        @Override
        public BuilderMap<TKey> saveSnapshot() {
            this.snapshot = new BuilderMap<>(this, this.target, false);
            return this;
        }

        /// 不建议真的在非string的情况下调用🤓
        public BuilderMap<TKey> buildInto(TKey key) {
            this.target.put(key, this.addStr(key.toString()).build());
            this.restoreSnapshot();
            return this;
        }

        public BuilderMap<TKey> buildIntoPlain(TKey key) {
            this.target.put(key, this.build());
            this.restoreSnapshot();
            return this;
        }

        public Map<TKey, Component> getMap() {
            return this.target;
        }
    }
}
