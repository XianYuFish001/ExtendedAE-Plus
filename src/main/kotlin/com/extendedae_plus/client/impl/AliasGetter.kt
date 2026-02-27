package com.extendedae_plus.client.impl

import com.electronwill.nightconfig.core.file.FileConfig
import com.electronwill.nightconfig.toml.TomlFormat
import com.extendedae_plus.integration.helper.ContextModLoaded
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationJech
import com.extendedae_plus.integration.impl.recipeViewer.emi.EmiRecipeAdaptable.Companion.unboxJemi
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mojang.logging.LogUtils
import dev.emi.emi.api.EmiApi
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.stack.EmiIngredient
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.item.crafting.RecipeHolder
import net.neoforged.fml.loading.FMLPaths
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.Map
import kotlin.concurrent.Volatile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

object AliasGetter {
    private val LOGGER = LogUtils.getLogger()
    private val PathConfig = FMLPaths.CONFIGDIR.get().resolve("extendedae_plus/stored_alias.toml")
    private val PathConfigOld = PathConfig.parent.resolve("stored_alias.json")
    private val Config = FileConfig
        .builder(PathConfig, TomlFormat.instance())
        .autosave()
        .autoreload()
        .build()

    init {
        Config.load()

        try {
            if (PathConfigOld.exists()) this.convertConfig()
        } catch (exception: IOException) {
            LOGGER.error("Failed to convert config, ", exception)
        }
    }

    @JvmStatic
    fun closeConfig() = Config.close()

    @Throws(IOException::class)
    private fun convertConfig() {
        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        val objectJson = gson.fromJson(Files.readString(PathConfigOld), JsonObject::class.java) ?: return

        objectJson.entrySet()
            .filter { entry ->
                val value = entry.value ?: return@filter false
                if (!value.isJsonPrimitive) return@filter false
                val valueString = value.asString
                valueString != null && !valueString.isBlank()
            }
            .forEach { entry ->
                Config.set(
                    Collections.singletonList(entry.key),
                    entry.value.asString
                )
            }
        Config.save()
        PathConfigOld.deleteIfExists()
    }

    /**
     * 向配置中新增或更新别名映射，并刷新内存映射。
     * 
     * @param typeKey 最终搜索关键字（不含冒号），大小写不敏感
     * @param alias   别名
     * @return 是否写入成功
     */
    @JvmStatic
    @Synchronized
    fun addOrUpdateAlias(typeKey: String?, alias: String?): Boolean {
        if (typeKey.isNullOrBlank() || alias.isNullOrBlank()) return false

        Config.set<String>(Collections.singletonList(typeKey.lowercase()), alias)
        return true
    }

    @JvmStatic
    @Synchronized
    fun removeAliases(alias: String?): Int {
        if (alias == null) return 0

        val target = alias.trim()
        if (target.isBlank()) return 0

        val toRemove = Config.entrySet()
            .map { it.getValue<String>() }
            .filter { it.equals(target, ignoreCase = true) }
        toRemove.forEach { Config.remove(it) }
        return toRemove.size
    }

    fun findMapping(key: String?): String? {
        if (key.isNullOrBlank()) return null

        return Config.get<String>(Collections.singletonList(key.lowercase()))
    }

    /** 收集到处理配方的关键词（按优先级排序） */
    @JvmField
    @Volatile
    var recipeKeywords: MutableList<KeywordGroup> = object : ArrayList<KeywordGroup>() {
        override fun clear() {
            keyUsed = false
            super.clear()
        }
    }
    var keyUsed: Boolean = false

    @JvmStatic
    fun getRecipeKeywords(): MutableList<KeywordGroup> {
        keyUsed = true
        recipeKeywords.sortWith(
            Comparator
                .comparing(KeywordGroup::mapped).reversed()
                .thenComparing(
                    (KeywordGroup::priority),
                    Comparator.reverseOrder()
                )
        )
        return recipeKeywords
    }

    fun collectRecipeKeyword(name: String, priority: Int, findMapping: Boolean) {
        if (keyUsed) recipeKeywords.clear()
        val group = KeywordGroup.literal(name)
        group.priority = priority
        if (findMapping) group.findMapping(true)
        recipeKeywords.add(group)
    }

    /**
     * @param recipe (J)EmiRecipe或RecipeHolder
     */
    @JvmStatic
    fun tryCollectKeywords(recipe: Any?) {
        recipeKeywords.clear()
        if (recipe == null) return
        val keys = HashMap<String, Int>()

        if (ContextModLoaded.Emi()) {
            var workstations: MutableList<EmiIngredient>
            var categoryName: Component

            val recipeJemi = unboxJemi(recipe)
            if (recipeJemi != null) {
                workstations = EmiApi.getRecipeManager().getWorkstations(recipeJemi.recipeCategory)
                categoryName = recipeJemi.category.title

                keys[recipeJemi.category.title.string] = 3
                if (recipeJemi.originalId != null) {
                    keys[recipeJemi.originalId.toString().split("/".toRegex())[0]] = 2
                    keys[recipeJemi.originalId.path.split("/".toRegex())[0]] = 1
                }
            } else if (recipe is EmiRecipe) {
                workstations = EmiApi.getRecipeManager().getWorkstations(recipe.category)
                categoryName = recipe.category.name

                keys[recipe.category.name.string] = 3
                if (recipe.id != null) {
                    keys[recipe.id.toString().split("/".toRegex())[0]] = 2
                    keys[recipe.id!!.path.split("/".toRegex())[0]] = 1
                }
            } else {
                workstations = Collections.emptyList()
                categoryName = Component.empty()
            }

            if (!workstations.isEmpty()) {
                val workstationKeys = ArrayList<String>()
                workstations.reversed().forEach { ingredient ->
                    ingredient.emiStacks.reversed().forEach { stack ->
                        val name = stack.name
                        workstationKeys.add(name.string)

                        val key = when (val contents = name.contents) {
                            is PlainTextContents -> contents.text()
                            is TranslatableContents -> contents.key
                            else -> return@forEach
                        }
                        workstationKeys += key
                    }
                }

                val groupWorkstation = KeywordGroup(
                    workstationKeys,
                    UtilKeyBuilder.of(Patterns.KeywordGroup)
                        .addStr("workstations")
                        .args(categoryName.string)
                        .build()
                )
                groupWorkstation.priority = 4
                groupWorkstation.findMapping(false)

                recipeKeywords += groupWorkstation
            }
        }

        if (recipe is RecipeHolder<*>) {
            keys[recipe.id().toString().split("/".toRegex())[0]] = 2
            keys[recipe.id().path.split("/".toRegex())[0]] = 1
        }

        keys.entries
            .filter { !it.key.isBlank() }
            .sortedWith(Map.Entry.comparingByValue<String, Int>(Comparator.reverseOrder<Int>()))
            .forEach {
                this.collectRecipeKeyword(
                    it.key,
                    it.value,
                    true
                )
            }
    }

    class KeywordGroup(keywords: MutableCollection<String>, groupDescription: Component) {
        private val keywords = ArrayList<String>()
        var description: Component
            private set
            get() = if (field.string.isEmpty())
                Component.literal(this.keywords[0])
            else field

        var mapped = false
            private set

        var priority = 0
            internal set

        val isEmpty: Boolean
            get() = keywords.isEmpty()

        init {
            this.keywords.addAll(keywords)
            this.description = groupDescription
        }

        fun matches(nameKey: String, i18nKey: String): Boolean {
            if (this.keywords.all(String::isBlank)) return true

            return nameMatches(this.description.string, nameKey)
                    || this.keywords.all { key ->
                nameMatches(key, nameKey)
                        || i18nKeyMatches(key, i18nKey)
            }
        }

        fun findMapping(mappingKeywords: Boolean) {
            val mappedDesc = findMapping(this.description.string)
            if (!mappedDesc.isNullOrBlank()) {
                this.description = Component.literal(mappedDesc)
                this.mapped = true
            }

            if (!mappingKeywords) return

            var mapped = false
            val mappedList = this.keywords.map { keyword ->
                val mappedKey = findMapping(keyword)
                if (mappedKey != null) {
                    mapped = true
                    mappedKey
                } else keyword
            }
            if (mapped) this.mapped = true

            this.keywords.clear()
            this.keywords.addAll(mappedList)
        }

        override fun equals(other: Any?): Boolean {
            if (other !is KeywordGroup) return false
            return this.keywords == other.keywords
        }

        override fun hashCode() = Objects.hash(this.keywords, this.description)

        companion object {
            private val literalGroups = HashMap<String, KeywordGroup>()

            @JvmStatic
            fun literal(value: String) = this.literalGroups.computeIfAbsent(value) {
                KeywordGroup(
                    Collections.singletonList(value),
                    Component.empty()
                )
            }

            private fun nameMatches(matchKey: String?, searchKey: String?): Boolean {
                if (matchKey.isNullOrBlank()) return false
                if (searchKey.isNullOrBlank()) return true

                var matches: Boolean

                val jech = ManagerIntegration<IntegrationJech>()
                matches = jech?.contains(matchKey, searchKey) == true
                matches = matches || jech?.contains(searchKey, matchKey) == true

                matches = matches || matchKey.contains(searchKey, ignoreCase = true)
                matches = matches || searchKey.contains(matchKey, ignoreCase = true)

                return matches
            }

            private fun i18nKeyMatches(matchKey: String?, searchKey: String?): Boolean {
                if (matchKey.isNullOrBlank() || searchKey.isNullOrBlank()) return false
                return matchKey.contains(searchKey, ignoreCase = true)
                        || searchKey.contains(matchKey, ignoreCase = true)
            }
        }
    }
}
