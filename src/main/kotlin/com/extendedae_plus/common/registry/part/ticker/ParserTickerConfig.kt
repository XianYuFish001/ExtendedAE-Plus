package com.extendedae_plus.common.registry.part.ticker

import com.extendedae_plus.EAEPConfig
import com.mojang.datafixers.util.Pair
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.Tuple
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import java.util.concurrent.locks.ReentrantReadWriteLock

object ParserTickerConfig {
    private val blacklistedBlocks = HashSet<SettingEntry>()
    private val multipliedBlocks = HashSet<Tuple<SettingEntry, Double>>()

    private var cachedSettings: Pair<List<String>, List<String>>? = null

    private val lock = ReentrantReadWriteLock()

    @JvmStatic
    fun isBlockBlacklisted(blockState: BlockState): Boolean {
        lock.readLock().lock()
        try {
            for (entry in blacklistedBlocks) if (entry.match(blockState)) return true
            return false
        } finally {
            lock.readLock().unlock()
        }
    }

    @JvmStatic
    fun getBlockExternalMultiplier(blockState: BlockState): Double {
        lock.readLock().lock()
        try {
            for (entry in multipliedBlocks) if (entry.a.match(blockState)) return entry.b
            return 1.0
        } finally {
            lock.readLock().unlock()
        }
    }

    fun clear() {
        lock.writeLock().lock()
        try {
            blacklistedBlocks.clear()
            multipliedBlocks.clear()
            cachedSettings = null
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun parseSettings() {
        lock.writeLock().lock()
        try {
            if (cachedSettings != null
                && cachedSettings?.first == EAEPConfig.TickerBlacklist
                && cachedSettings?.second == EAEPConfig.TickerExternalMultiplier
            ) return
            cachedSettings = Pair(EAEPConfig.TickerBlacklist, EAEPConfig.TickerExternalMultiplier)

            blacklistedBlocks.clear()
            multipliedBlocks.clear()

            EAEPConfig.TickerBlacklist.normalize()
                .forEach(ParserTickerConfig::parseBlacklistEntry)
            EAEPConfig.TickerExternalMultiplier.normalize()
                .forEach(ParserTickerConfig::parseMultiplierEntry)
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun parseBlacklistEntry(value: String) {
        val parsedValue: SettingEntry

        val flagTag = value.startsWith("#")
        val location = ResourceLocation.tryParse(
            value.substring(if (flagTag) 1 else 0)
        )
        if (location == null) return

        if (flagTag) {
            parsedValue = SettingEntry(null, TagKey.create(Registries.BLOCK, location))
        } else {
            for (existingEntry in blacklistedBlocks) if (existingEntry.match(location)) return
            parsedValue = SettingEntry(location, null)
        }
        blacklistedBlocks.add(parsedValue)
    }

    private fun parseMultiplierEntry(value: String) {
        val multiplierStart = value.lastIndexOf("[")
        val multiplierEnd = value.lastIndexOf("]")
        if (multiplierStart > multiplierEnd || multiplierEnd != value.length - 1) return

        val parsedValue: Tuple<SettingEntry, Double> = Tuple<SettingEntry, Double>(null, 1.0)

        // Part Location
        val flagTag = value.startsWith("#")

        val location = ResourceLocation.tryParse(value.substring(if (flagTag) 1 else 0, multiplierStart))
        if (location == null) return

        if (flagTag) {
            parsedValue.a = SettingEntry(null, TagKey.create(Registries.BLOCK, location))
        } else {
            for (existingEntry in multipliedBlocks) if (existingEntry.getA().match(location)) return
            parsedValue.a = SettingEntry(location, null)
        }

        // Part Multiplier
        try {
            val multiplier = value.substring(multiplierStart + 1, multiplierEnd).toDouble()
            if (multiplier <= 0.0) return
            parsedValue.b = multiplier
        } catch (_: NumberFormatException) {
            return
        }

        multipliedBlocks.add(parsedValue)
    }

    private fun List<String?>.normalize() = this
            .filterNotNull()
            .filter(String::isNotBlank)
            .map(String::trim)

    /**
     * params: Either
     */
    @JvmRecord
    private data class SettingEntry(val location: ResourceLocation?, val tagKey: TagKey<Block>?) {
        fun match(blockState: BlockState): Boolean {
            if (this.location == null) return blockState.`is`(this.tagKey ?: return false)
            return blockState.`is`(BuiltInRegistries.BLOCK.get(this.location))
        }

        fun match(location: ResourceLocation) =
            this.match(BuiltInRegistries.BLOCK.get(location).defaultBlockState())
    }
}
