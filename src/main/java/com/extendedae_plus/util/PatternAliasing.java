package com.extendedae_plus.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PatternAliasing {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CONFIG_RELATIVE = "extendedae_plus/stored_alias.json";
    private static final Map<String, String> ALIASES = new ConcurrentHashMap<>();

    static {
        try {
            loadAliases();
        } catch (Throwable ignore) {}
    }

    private static synchronized void createAliasTemplate(Path filePath) throws IOException {
        if (Files.exists(filePath)) return;

        Files.createDirectories(filePath.getParent());
        JsonObject tmpl = new JsonObject();
        tmpl.addProperty("minecraft:smelting", "熔炉");
        tmpl.addProperty("minecraft:blasting", "高炉");
        tmpl.addProperty("minecraft:smoking", "烟熏");
        tmpl.addProperty("minecraft:campfire_cooking", "营火");
        Files.writeString(filePath, GSON.toJson(tmpl));
    }

    /**
     * 从配置文件加载 RecipeType → 中文名称映射。文件不存在则生成模板。
     * 同时支持“别名”形式：不含冒号的键会被视为最终搜索关键字（大小写不敏感），如：
     * {
     *   "assembler": "组装机"
     * }
     */
    public static synchronized void loadAliases() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get();
            Path filePath = configPath.resolve(CONFIG_RELATIVE);

            if (!Files.exists(filePath)) createAliasTemplate(filePath);

            String json = Files.readString(filePath);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            if (obj == null) {
                ALIASES.clear();
                return;
            }

            Map<String, String> resolvedAlias = new HashMap<>();

            obj.entrySet().forEach(entry -> {
                var typeKey = entry.getKey();
                var aliasValve = entry.getValue();
                if (aliasValve == null || !aliasValve.isJsonPrimitive()) return;

                var aliasName = aliasValve.getAsString();
                if (aliasName == null || aliasName.isBlank()) return;

                resolvedAlias.put(typeKey.toLowerCase(), aliasName);
            });

            ALIASES.clear();
            ALIASES.putAll(resolvedAlias);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 向配置中新增或更新“别名 -> 中文”映射，并刷新内存映射。
     * 仅用于非原版（或希望使用最终搜索关键字）场景。
     *
     * @param typeKey 最终搜索关键字（不含冒号），大小写不敏感
     * @param alias  中文名称
     * @return 是否写入成功
     */
    public static synchronized boolean addOrUpdateAlias(String typeKey, String alias) {
        if (typeKey == null || typeKey.isBlank() || alias == null || alias.isBlank())
            return false;

        try {
            Path configPath = FMLPaths.CONFIGDIR.get();
            Path filePath = configPath.resolve(CONFIG_RELATIVE);

            if (!Files.exists(filePath)) createAliasTemplate(filePath);

            JsonObject obj;
            if (Files.exists(filePath)) {
                String json = Files.readString(filePath);
                obj = GSON.fromJson(json, JsonObject.class);
                if (obj == null) obj = new JsonObject();
            } else return false;

            String key = typeKey.trim();

            obj.addProperty(key, alias);
            Files.writeString(filePath, GSON.toJson(obj));

            ALIASES.put(key.toLowerCase(), alias);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 按中文值精确匹配删除映射（支持别名与完整ID）。
     * 返回删除的条目数量。
     */
    public static synchronized int removeAliases(String alias) {
        if (alias == null) return 0;

        String target = alias.trim();
        if (target.isBlank()) return 0;

        try {
            Path configPath = FMLPaths.CONFIGDIR.get();
            Path filePath = configPath.resolve(CONFIG_RELATIVE);
            if (!Files.exists(filePath)) {
                createAliasTemplate(filePath);
                return 0;
            }

            String json = Files.readString(filePath);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            if (obj == null) return 0;

            List<String> toRemove = new ArrayList<>();
            obj.entrySet().forEach(entry -> {
                var aliasValue = entry.getValue();
                if (aliasValue == null || !aliasValue.isJsonPrimitive()) return;

                var aliasName = aliasValue.getAsString();
                if (target.equalsIgnoreCase(aliasName))
                    toRemove.add(entry.getKey().toLowerCase());
            });
            if (toRemove.isEmpty()) return 0;

            toRemove.forEach(ALIASES::remove);
            toRemove.forEach(obj::remove);
            Files.writeString(filePath, GSON.toJson(obj));
            return toRemove.size();
        } catch (IOException e) {
            return 0;
        }
    }

    public static String findMapping(String key) {
        if (key == null || key.isBlank()) return null;

        if (ALIASES.containsKey(key.toLowerCase()))
            return ALIASES.get(key.toLowerCase());

        return null;
    }

    /// 收集到处理配方的关键词（按优先级排序）
    public static volatile List<KeywordGroup> recipeKeywords = new ArrayList<>();

    public static void collectRecipeKeyword(String name, int priority, boolean findMapping) {
        var group = new KeywordGroup(name);
        group.setPriority(priority);
        if (findMapping) group.findMapping(true);
        recipeKeywords.add(group);
    }

    /// @param recipe (J)EmiRecipe或RecipeHolder
    public static void tryCollectKeywords(Object recipe) {
        if (recipe == null) return;
        var keys = new HashMap<String, Integer>();

        if (ModList.get().isLoaded("emi")) {
            List<EmiIngredient> workstations = new ArrayList<>();
            Component categoryName = Component.empty();

            if (recipe instanceof JemiRecipe<?> jemiRecipe) {
                workstations = EmiApi.getRecipeManager().getWorkstations(jemiRecipe.recipeCategory);
                categoryName = jemiRecipe.category.getTitle();

                keys.put(jemiRecipe.category.getTitle().getString(), 3);
                keys.put(jemiRecipe.originalId.toString().split("/")[0], 2);
                keys.put(jemiRecipe.originalId.getPath().split("/")[0], 1);
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
                            workstationKeys.add(stack.getName().getString());
                            workstationKeys.add(stack.getName().toString());
                        })
                );

                var workstationGroup = new KeywordGroup(workstationKeys,
                        Component.translatable("keyword_group.extendedae_plus.workstations", categoryName));
                workstationGroup.setPriority(4);
                workstationGroup.findMapping(false);

                recipeKeywords.add(workstationGroup);
            }
        }

        if (recipe instanceof RecipeHolder<?> recipeHolder){
            keys.put(recipeHolder.id().toString().split("/")[0], 2);
            keys.put(recipeHolder.id().getPath().split("/")[0], 1);
        }

        keys.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> collectRecipeKeyword(entry.getKey(), entry.getValue(), true));
    }

    public static class KeywordGroup {
        public static final KeywordGroup EMPTY = new KeywordGroup("");

        private final List<String> keywords = new ArrayList<>();
        private Component description;
        private int priority = 0;

        public KeywordGroup(String keywords) {
            this(List.of(keywords), Component.empty());
        }

        public KeywordGroup(Collection<String> keywords, Component groupDescription) {
            this.keywords.addAll(keywords);
            this.description = groupDescription;
        }

        public boolean match(String nameKey, String i18nKey) {
            if (this.keywords.stream().map(String::isBlank).allMatch(Predicate.isEqual(true))) return true;

            boolean descMatched = nameMatches(this.description.getString(), nameKey);
            boolean keywordsMatched = keywords.stream().anyMatch(
                    key -> nameMatches(key, nameKey) || i18nKeyMatches(key, i18nKey));

            return descMatched || keywordsMatched;
        }

        private static boolean nameMatches(String matchKey, String searchKey) {
            if (matchKey == null || matchKey.isBlank()) return false;
            if (searchKey == null || searchKey.isBlank()) return true;

            if (ModList.get().isLoaded("jecharacters")) {
                try {
                    Method jecContains = Class.forName("me.towdium.jecharacters.utils.Match")
                            .getMethod("contains", CharSequence.class, CharSequence.class);
                    Object result = jecContains.invoke(null, matchKey, searchKey);
                    if (result instanceof Boolean resultBool) return resultBool;
                } catch (Throwable ignore) {
                }
            }

            return matchKey.toLowerCase().contains(searchKey.toLowerCase()) ||
                    searchKey.toLowerCase().contains(matchKey.toLowerCase());
        }

        private static boolean i18nKeyMatches(String matchKey, String searchKey) {
            if (matchKey == null || matchKey.isBlank() ||
                    searchKey == null || searchKey.isEmpty()) return false;
            return matchKey.toLowerCase().contains(searchKey.toLowerCase()) ||
                    searchKey.toLowerCase().contains(matchKey.toLowerCase());
        }

        public Component getDescription() {
            if ("empty".equals(this.description.toString()))
                return Component.literal(keywords.getFirst());
            else return this.description;
        }

        public void findMapping(boolean mappingKeywords) {
            var mappedDesc = PatternAliasing.findMapping(this.description.getString());
            if (mappedDesc != null && !mappedDesc.isBlank()) {
                this.description = Component.literal(mappedDesc);
            }

            if (!mappingKeywords) return;

            var mappedList = this.keywords.stream().map(keyword -> {
                var mapped = PatternAliasing.findMapping(keyword);
                return mapped != null ? mapped : keyword;
            }).toList();

            this.keywords.clear();
            this.keywords.addAll(mappedList);
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeywordGroup other)) return false;
            return this.keywords.equals(other.keywords);
        }

        public boolean isEmpty() {
            return keywords.isEmpty();
        }
    }
}
