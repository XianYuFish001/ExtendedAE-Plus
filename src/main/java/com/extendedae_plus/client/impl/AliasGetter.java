package com.extendedae_plus.client.impl;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.extendedae_plus.integration.ContextModLoaded;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiRecipe;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class AliasGetter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path pathConfig = FMLPaths.CONFIGDIR.get().resolve("extendedae_plus/stored_alias.toml");
    private static final Path pathConfigOld = pathConfig.getParent().resolve("stored_alias.json");
    private static final FileConfig config = FileConfig.builder(pathConfig, TomlFormat.instance()).autosave().autoreload().build();

    static {
        config.load();

        try {
            if (Files.exists(pathConfigOld))
                convertConfig();
        } catch (IOException exception) {
            LOGGER.error("Failed to convert config, ", exception);
        }
    }

    public static void closeConfig() {
        config.close();
    }

    private static void convertConfig() throws IOException {
        var gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        final var obj = gson.fromJson(Files.readString(pathConfigOld), JsonObject.class);
        if (obj == null) return;

        obj.entrySet().stream()
                .filter(entry -> {
                    var value = entry.getValue();
                    if (value == null || !value.isJsonPrimitive()) return false;
                    var valueString = value.getAsString();
                    return valueString != null && !valueString.isBlank();
                })
                .forEach(entry -> config.set(List.of(entry.getKey()), entry.getValue().getAsString()));
        config.save();
        Files.deleteIfExists(pathConfigOld);
    }

    /**
     * 向配置中新增或更新别名映射，并刷新内存映射。
     *
     * @param typeKey 最终搜索关键字（不含冒号），大小写不敏感
     * @param alias   别名
     * @return 是否写入成功
     */
    public static synchronized boolean addOrUpdateAlias(String typeKey, String alias) {
        if (typeKey == null || typeKey.isBlank() || alias == null || alias.isBlank())
            return false;

        config.set(List.of(typeKey.toLowerCase()), alias);
        return true;
    }

    public static synchronized int removeAliases(String alias) {
        if (alias == null) return 0;

        var target = alias.trim();
        if (target.isBlank()) return 0;

        var toRemove = config.entrySet().stream()
                .filter(entry -> entry.getValue().toString().equalsIgnoreCase(target))
                .map(Config.Entry::getKey)
                .toList();
        toRemove.forEach(config::remove);
        return toRemove.size();
    }

    public static @Nullable String findMapping(String key) {
        if (key == null || key.isBlank()) return null;

        return config.get(List.of(key.toLowerCase()));
    }

    /// 收集到处理配方的关键词（按优先级排序）
    public static volatile List<KeywordGroup> recipeKeywords = new ArrayList<>() {
        @Override
        public void clear() {
            keyUsed = false;
            super.clear();
        }
    };
    public static boolean keyUsed = false;

    public static List<KeywordGroup> getRecipeKeywords() {
        keyUsed = true;
        recipeKeywords.sort(Comparator
                .comparing(KeywordGroup::isMapped).reversed()
                .thenComparing(KeywordGroup::getPriority, Comparator.reverseOrder()));
        return recipeKeywords;
    }

    public static void collectRecipeKeyword(String name, int priority, boolean findMapping) {
        if (keyUsed) recipeKeywords.clear();
        var group = KeywordGroup.literal(name);
        group.setPriority(priority);
        if (findMapping) group.findMapping(true);
        recipeKeywords.add(group);
    }

    /// @param recipe (J)EmiRecipe或RecipeHolder
    public static void tryCollectKeywords(Object recipe) {
        recipeKeywords.clear();
        if (recipe == null) return;
        var keys = new HashMap<String, Integer>();

        if (ContextModLoaded.emi.isLoaded()) {
            List<EmiIngredient> workstations = new ArrayList<>();
            Component categoryName = Component.empty();

            if (recipe instanceof JemiRecipe<?> jemiRecipe) {
                workstations = EmiApi.getRecipeManager().getWorkstations(jemiRecipe.recipeCategory);
                categoryName = jemiRecipe.category.getTitle();

                keys.put(jemiRecipe.category.getTitle().getString(), 3);
                if (jemiRecipe.originalId != null) {
                    keys.put(jemiRecipe.originalId.toString().split("/")[0], 2);
                    keys.put(jemiRecipe.originalId.getPath().split("/")[0], 1);
                }
            } else if (recipe instanceof EmiRecipe emiRecipe) {
                workstations = EmiApi.getRecipeManager().getWorkstations(emiRecipe.getCategory());
                categoryName = emiRecipe.getCategory().getName();

                keys.put(emiRecipe.getCategory().getName().getString(), 3);
                if (emiRecipe.getId() != null) {
                    keys.put(emiRecipe.getId().toString().split("/")[0], 2);
                    keys.put(emiRecipe.getId().getPath().split("/")[0], 1);
                }
            }

            if (!workstations.isEmpty()) {
                var workstationKeys = new ArrayList<String>();
                workstations.reversed().forEach(ingredient -> ingredient.getEmiStacks().reversed()
                        .forEach(stack -> {
                            var name = stack.getName();
                            workstationKeys.add(name.getString());

                            String key = null;
                            if (name.getContents() instanceof PlainTextContents contents)
                                key = contents.text();
                            else if (name.getContents() instanceof TranslatableContents contents)
                                key = contents.getKey();
                            if (key == null) return;
                            workstationKeys.add(key);
                        })
                );

                var groupWorkstation = new KeywordGroup(workstationKeys,
                        UtilKeyBuilder.of(UtilKeyBuilder.keywordGroup)
                                .addStr("workstations")
                                .args(categoryName.getString())
                                .build());
                groupWorkstation.setPriority(4);
                groupWorkstation.findMapping(false);

                recipeKeywords.add(groupWorkstation);
            }
        }

        if (recipe instanceof RecipeHolder<?> recipeHolder) {
            keys.put(recipeHolder.id().toString().split("/")[0], 2);
            keys.put(recipeHolder.id().getPath().split("/")[0], 1);
        }

        keys.entrySet().stream()
                .filter(entry -> !entry.getKey().isBlank())
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> collectRecipeKeyword(entry.getKey(), entry.getValue(), true));
    }

    public static class KeywordGroup {
        private static final HashMap<String, KeywordGroup> literalGroups = new HashMap<>();

        private final List<String> keywords = new ArrayList<>();
        private Component description;
        @Getter
        private boolean mapped = false;
        @Setter
        @Getter
        private int priority = 0;

        public KeywordGroup(Collection<String> keywords, Component groupDescription) {
            this.keywords.addAll(keywords);
            this.description = groupDescription;
        }

        public static KeywordGroup literal(String value) {
            return literalGroups.computeIfAbsent(value, $ ->
                    new KeywordGroup(List.of(value), Component.empty()));
        }

        public boolean matches(String nameKey, String i18nKey) {
            if (this.keywords.stream().map(String::isBlank)
                    .allMatch(Predicate.isEqual(true)))
                return true;

            return nameMatches(this.description.getString(), nameKey)
                    || this.keywords.stream().anyMatch(
                    key -> nameMatches(key, nameKey)
                            || i18nKeyMatches(key, i18nKey));
        }

        private static boolean nameMatches(String matchKey, String searchKey) {
            if (matchKey == null || matchKey.isBlank()) return false;
            if (searchKey == null || searchKey.isBlank()) return true;

            var jechMatches = false;
            if (ContextModLoaded.jech.isLoaded()) {
                try {
                    var methodContains = Class.forName("me.towdium.jecharacters.utils.Match")
                            .getMethod("contains", String.class, CharSequence.class);
                    jechMatches = (boolean) methodContains.invoke(
                            null, searchKey.toLowerCase(), matchKey.toLowerCase());
                } catch (Throwable ignore) {
                }
            }

            return jechMatches
                    || matchKey.toLowerCase().contains(searchKey.toLowerCase())
                    || searchKey.toLowerCase().contains(matchKey.toLowerCase());
        }

        private static boolean i18nKeyMatches(String matchKey, String searchKey) {
            if (matchKey == null || matchKey.isBlank() ||
                    searchKey == null || searchKey.isEmpty()) return false;
            return matchKey.toLowerCase().contains(searchKey.toLowerCase()) ||
                    searchKey.toLowerCase().contains(matchKey.toLowerCase());
        }

        public Component getDescription() {
            if (this.description.getString().isEmpty())
                return Component.literal(this.keywords.getFirst());
            else return this.description;
        }

        public void findMapping(boolean mappingKeywords) {
            var mappedDesc = AliasGetter.findMapping(this.description.getString());
            if (mappedDesc != null && !mappedDesc.isBlank()) {
                this.description = Component.literal(mappedDesc);
                this.mapped = true;
            }

            if (!mappingKeywords) return;

            var mapped = new boolean[]{false};
            var mappedList = this.keywords.stream().map(keyword -> {
                var mappedKey = AliasGetter.findMapping(keyword);
                if (mappedKey != null) {
                    mapped[0] = true;
                    return mappedKey;
                } else return keyword;
            }).toList();
            if (mapped[0]) this.mapped = true;

            this.keywords.clear();
            this.keywords.addAll(mappedList);
        }

        public boolean isEmpty() {
            return keywords.isEmpty();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeywordGroup other)) return false;
            return this.keywords.equals(other.keywords);
        }
    }
}
