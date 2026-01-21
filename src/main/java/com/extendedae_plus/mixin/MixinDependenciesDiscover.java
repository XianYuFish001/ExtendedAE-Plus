package com.extendedae_plus.mixin;

import com.extendedae_plus.util.UtilFormat;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MixinDependenciesDiscover {
    private static final Logger LOGGER = LoggerFactory.getLogger("mixin/ExtendedAEPlus/dependenciesDiscover");
    private static final Map<String, Boolean> modStatus = new HashMap<>();
    private static final Map<String, Boolean> mixinDependencyCache = new HashMap<>();
    private static ClassLoader storedLoader = null;

    private static boolean isModLoaded(String modID) {
        if (modStatus.containsKey(modID)) return modStatus.get(modID);

        boolean loaded;
        if (ModList.get() == null)
            loaded = LoadingModList.get().getMods().stream()
                    .map(ModInfo::getModId)
                    .anyMatch(Predicate.isEqual(modID));
        else loaded = ModList.get().isLoaded(modID);
        modStatus.put(modID, loaded);
        return loaded;
    }

    static boolean checkMixinDependencies(String mixinClassName) {
        if (mixinDependencyCache.containsKey(mixinClassName))
            return mixinDependencyCache.get(mixinClassName);

        MixinDependencies.DependencyInfo dependencies = getDependencyInfo(mixinClassName);

        if (!dependencies.hasAnnotation) {
            mixinDependencyCache.put(mixinClassName, true);
            return true;
        }

        LOGGER.debug("Found @MixinDependencies" +
                "{MixinClass[{}], requireMods{}, conflictMods{}}",
                UtilFormat.splitToLastKey(mixinClassName, "core", "\\."),
                dependencies.requiredMods, dependencies.conflictMods);

        for (String requiredMod : dependencies.requiredMods) {
            if (!isModLoaded(requiredMod)) {
                mixinDependencyCache.put(mixinClassName, false);
                return false;
            }
        }

        for (String conflictMod : dependencies.conflictMods) {
            if (isModLoaded(conflictMod)) {
                mixinDependencyCache.put(mixinClassName, false);
                return false;
            }
        }

        mixinDependencyCache.put(mixinClassName, true);
        return true;
    }

    private static MixinDependencies.DependencyInfo getDependencyInfo(String mixinClassName) {
        MixinDependencies.DependencyInfo dependencies = new MixinDependencies.DependencyInfo();
        String classPath = mixinClassName.replace('.', '/') + ".class";

        if (storedLoader == null) storedLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream is = storedLoader.getResourceAsStream(classPath)) {
            if (is != null) {
                ClassReader classReader = new ClassReader(is);
                classReader.accept(new DependenciesClassVisitor(dependencies), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        } catch (IOException e) {
            LOGGER.warn("[EAEP/mixin] Failed to read mixin class: {}", mixinClassName);
        }

        return dependencies;
    }

    private static class DependenciesClassVisitor extends ClassVisitor {
        private final MixinDependencies.DependencyInfo dependencies;

        public DependenciesClassVisitor(MixinDependencies.DependencyInfo dependencies) {
            super(Opcodes.ASM9);
            this.dependencies = dependencies;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if ("Lcom/extendedae_plus/mixin/MixinDependencies;".equals(descriptor)) {
                dependencies.hasAnnotation = true;
                return new DependenciesAnnotationVisitor(dependencies);
            }
            return null;
        }
    }

    private static class DependenciesAnnotationVisitor extends AnnotationVisitor {
        private final MixinDependencies.DependencyInfo dependencies;

        public DependenciesAnnotationVisitor(MixinDependencies.DependencyInfo dependencies) {
            super(Opcodes.ASM9);
            this.dependencies = dependencies;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            if ("value".equals(name) || "conflict".equals(name))
                return new DependenciesValuesVisitor(name, dependencies);
            return null;
        }
    }

    private static class DependenciesValuesVisitor extends AnnotationVisitor {
        private final String arrayName;
        private final MixinDependencies.DependencyInfo dependencies;

        public DependenciesValuesVisitor(String arrayName, MixinDependencies.DependencyInfo dependencies) {
            super(Opcodes.ASM9);
            this.arrayName = arrayName;
            this.dependencies = dependencies;
        }

        @Override
        public void visit(String name, Object value) {
            if (!(value instanceof String nameMod)) return;

            if ("value".equals(arrayName))
                dependencies.requiredMods.add(nameMod);
            else if ("conflict".equals(arrayName))
                dependencies.conflictMods.add(nameMod);
        }
    }
}
