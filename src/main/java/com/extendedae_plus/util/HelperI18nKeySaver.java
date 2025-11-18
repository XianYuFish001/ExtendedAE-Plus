package com.extendedae_plus.util;

import com.extendedae_plus.ExtendedAEPlus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
/// \*Test Only\*
public class HelperI18nKeySaver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean testEnv = "dev".equals(System.getProperty("extendedae_plus.environment"));

    private static final Set<String> GENERATED_KEYS = Collections.synchronizedSet(new LinkedHashSet<>());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final File OUTPUT_FILE = Paths.get(
            "..", "GeneratedTranslationKeys.json")
            .normalize().toAbsolutePath().toFile();

    static {
        if (testEnv) loadExistingKeys();
    }
    
    public static void recordKey(String key, Object... args) {
        if (testEnv && key != null && !key.isEmpty()) {
            GENERATED_KEYS.add(key + ',' + Arrays.toString(args));
        }
    }

    private static void loadExistingKeys() {
        if (!OUTPUT_FILE.exists()) {
            LOGGER.info("[EAEP/test] 未找到现有翻译键文件，将创建新文件");
            return;
        }

        try (FileReader reader = new FileReader(OUTPUT_FILE)) {
            Type setType = new TypeToken<String[]>() {}.getType();
            String[] existingKeys = GSON.fromJson(reader, setType);

            if (existingKeys != null && existingKeys.length > 0) {
                Collections.addAll(GENERATED_KEYS, existingKeys);
                LOGGER.info("[EAEP/test] 成功加载 {} 个现有翻译键", existingKeys.length);
            } else {
                LOGGER.info("[EAEP/test] 现有翻译键文件为空");
            }
        } catch (IOException e) {
            LOGGER.warn("[EAEP/test/warn] 读取现有翻译键文件失败: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.warn("[EAEP/test/warn] 解析现有翻译键文件失败: {}", e.getMessage());
        }
    }


    @SubscribeEvent
    public static void dumpKeysToFile(GameShuttingDownEvent event) {
        if (!testEnv || GENERATED_KEYS.isEmpty()) {
            return;
        }

        try {
            File parentDir = OUTPUT_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                Files.createDirectories(parentDir.toPath());
            }

            String[] sortedKeys = GENERATED_KEYS.stream()
                    .sorted()
                    .toArray(String[]::new);

            try (FileWriter writer = new FileWriter(OUTPUT_FILE)) {
                GSON.toJson(sortedKeys, writer);
                LOGGER.info("[EAEP/test] 成功生成 {} 个翻译键到: {}", sortedKeys.length, OUTPUT_FILE.getAbsolutePath());
            }
        } catch (IOException ignore) {
            LOGGER.warn("[EAEP/test/warn]写入翻译键文件失败: {}", OUTPUT_FILE.getAbsolutePath());
        }
    }
}
