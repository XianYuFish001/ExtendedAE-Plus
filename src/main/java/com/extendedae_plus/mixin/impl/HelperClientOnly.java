package com.extendedae_plus.mixin.impl;

import java.lang.reflect.InvocationTargetException;

public interface HelperClientOnly {
    HelperClientOnly instance = getInstance();

    private static HelperClientOnly getInstance() {
        try {
            return (HelperClientOnly) Class.forName("com.extendedae_plus.mixin.impl.instance.ImplClientOnly")
                    .getConstructor()
                    .newInstance();
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException exception) {
            throw new IllegalStateException("Could not find instance for HelperClientOnly", exception);
        }
    }

    boolean hasControlDown();

    boolean hasShiftDown();
}
