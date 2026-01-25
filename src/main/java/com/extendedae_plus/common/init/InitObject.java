package com.extendedae_plus.common.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InitObject {
    int priority() default 100;

    class Initializer {
        private static final Logger LOGGER = LoggerFactory.getLogger("[EAEP/Initializer]");

        public static void init(IEventBus eventBus, ModContainer modContainer) {
            ModList.get().getAllScanData().stream()
                    .flatMap(data -> data.getAnnotations().stream())
                    .filter(data -> Type.getType(InitObject.class).equals(data.annotationType()))
                    .sorted(Comparator.comparing(data ->
                            (int) data.annotationData().getOrDefault("priority", 100)))
                    .forEach(data -> init(eventBus, modContainer, data));
        }

        private static void init(IEventBus eventBus, ModContainer modContainer, ModFileScanData.AnnotationData data) {
            Class<?> clazzObject;
            try {
                clazzObject = Class.forName(data.clazz().getClassName());
            } catch (ClassNotFoundException exception) {
                LOGGER.warn("Failed to load class {}", data.clazz().getClassName(), exception);
                return;
            }

            switch (data.targetType()) {
                case FIELD -> {
                    try {
                        if (!(clazzObject.getDeclaredField(data.memberName()).get(null)
                                instanceof DeferredRegister<?> register)) return;
                        register.register(eventBus);
                    } catch (NoSuchFieldException | IllegalAccessException exception) {
                        LOGGER.warn("Failed to load field register {}", data.clazz().getClassName(), exception);
                    }
                }
                case METHOD -> {
                    var valueMethod = data.memberName();
                    var nameMethod = valueMethod.substring(0, valueMethod.indexOf("("));

                    try {
                        Method method;
                        Object[] params;
                        if (valueMethod.contains("IEventBus") && valueMethod.contains("ModContainer")) {
                            method = clazzObject.getDeclaredMethod(nameMethod, IEventBus.class, ModContainer.class);
                            params = new Object[]{eventBus, modContainer};
                        } else if (valueMethod.contains("IEventBus")) {
                            method = clazzObject.getDeclaredMethod(nameMethod, IEventBus.class);
                            params = new Object[]{eventBus};
                        } else {
                            method = clazzObject.getDeclaredMethod(nameMethod);
                            params = new Object[0];
                        }
                        method.setAccessible(true);
                        method.invoke(null, params);
                    } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException exception) {
                        LOGGER.warn("Failed to load method init {}", data.clazz().getClassName(), exception);
                    }
                }
            }
        }
    }
}
