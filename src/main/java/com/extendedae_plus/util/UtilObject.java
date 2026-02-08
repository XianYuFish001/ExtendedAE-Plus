package com.extendedae_plus.util;

public class UtilObject {
    @SuppressWarnings("unchecked")
    public static <TType> TType cast(Object instance) {
        return ((TType) instance);
    }
}
