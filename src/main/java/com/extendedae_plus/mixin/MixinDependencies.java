package com.extendedae_plus.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * awc这depend注解控制怎么这么好用啊😋😋😋
 * <p>
 * awc这asm性能开销怎么这么大啊😭😭😭
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MixinDependencies {
    String[] value() default {};
    String[] conflict() default {};

    class DependencyInfo {
        boolean hasAnnotation = false;
        final List<String> requiredMods = new ArrayList<>();
        final List<String> conflictMods = new ArrayList<>();
    }
}