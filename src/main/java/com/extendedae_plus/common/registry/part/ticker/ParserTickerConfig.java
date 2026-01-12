package com.extendedae_plus.common.registry.part.ticker;

import com.extendedae_plus.EAEPConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ParserTickerConfig {
    private static final Set<SettingEntry> blacklistedBlocks = new HashSet<>();
    private static final Set<Tuple<SettingEntry, Double>> multipliedBlocks = new HashSet<>();

    private static Pair<List<? extends String>, List<? extends String>> cachedSettings =
            new Pair<>(List.of(), List.of());

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    static boolean isBlockBlacklisted(BlockState blockState) {
        lock.readLock().lock();
        try {
            for (var entry : blacklistedBlocks)
                if (entry.match(blockState)) return true;
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    static double getBlockExternalMultiplier(BlockState blockState) {
        lock.readLock().lock();
        try {
            for (var entry : multipliedBlocks)
                if (entry.getA().match(blockState)) return entry.getB();
            return 1D;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void clear() {
        lock.writeLock().lock();
        try {
            blacklistedBlocks.clear();
            multipliedBlocks.clear();
            cachedSettings = new Pair<>(List.of(), List.of());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void parseSettings() {
        lock.writeLock().lock();
        try {
            if (isEqual(cachedSettings.getFirst(), EAEPConfig.TICKER_BLACKLIST.get())
                    && isEqual(cachedSettings.getSecond(), EAEPConfig.TICKER_EXTERNAL_MULTIPLIER.get())) return;
            cachedSettings = new Pair<>(EAEPConfig.TICKER_BLACKLIST.get(), EAEPConfig.TICKER_EXTERNAL_MULTIPLIER.get());

            blacklistedBlocks.clear();
            multipliedBlocks.clear();

            normalizeList(EAEPConfig.TICKER_BLACKLIST.get()).forEach(ParserTickerConfig::parseBlacklistEntry);
            normalizeList(EAEPConfig.TICKER_EXTERNAL_MULTIPLIER.get()).forEach(ParserTickerConfig::parseMultiplierEntry);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void parseBlacklistEntry(String value) {
        SettingEntry parsedValue;

        boolean flagTag = value.startsWith("#");
        var location = ResourceLocation.tryParse(
                value.substring(flagTag ? 1 : 0));
        if (location == null) return;

        if (flagTag) {
            parsedValue = new SettingEntry(null, TagKey.create(Registries.BLOCK, location));
        } else {
            for (var existingEntry : blacklistedBlocks)
                if (existingEntry.match(location)) return;
            parsedValue = new SettingEntry(location, null);
        }
        blacklistedBlocks.add(parsedValue);
    }

    private static void parseMultiplierEntry(String value) {
        int multiplierStart = value.lastIndexOf("[");
        int multiplierEnd = value.lastIndexOf("]");
        if (multiplierStart > multiplierEnd || multiplierEnd != value.length() - 1) return;

        var parsedValue = new Tuple<SettingEntry, Double>(null, 1D);

        // Part Location
        boolean flagTag = value.startsWith("#");

        var location = ResourceLocation.tryParse(value.substring(flagTag ? 1 : 0, multiplierStart));
        if (location == null) return;

        if (flagTag) {
            parsedValue.setA(new SettingEntry(null, TagKey.create(Registries.BLOCK, location)));
        } else {
            for (var existingEntry : multipliedBlocks)
                if (existingEntry.getA().match(location)) return;
            parsedValue.setA(new SettingEntry(location, null));
        }

        // Part Multiplier
        try {
            var multiplier = Double.parseDouble(value.substring(multiplierStart + 1, multiplierEnd));
            if (multiplier <= 0D) return;
            parsedValue.setB(multiplier);
        } catch (NumberFormatException ignore) {
            return;
        }

        multipliedBlocks.add(parsedValue);
    }

    private static List<String> normalizeList(List<? extends String> raw) {
        return raw.stream()
                .filter(Objects::nonNull)
                .filter(string -> !string.isBlank())
                .map(String::trim)
                .toList();
    }

    private static boolean isEqual(List<?> listA, List<?> listB) {
        return Arrays.equals(listA.toArray(), listB.toArray());
    }

    private record SettingEntry(ResourceLocation location, TagKey<Block> tagKey) {
        boolean match(BlockState blockState) {
            if (this.location == null) return blockState.is(this.tagKey);
            return blockState.is(BuiltInRegistries.BLOCK.get(this.location));
        }

        boolean match(ResourceLocation location) {
            return this.match(BuiltInRegistries.BLOCK.get(location).defaultBlockState());
        }
    }
}
