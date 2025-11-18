package com.extendedae_plus.util;

import java.util.Arrays;

public class UtilFormat {
    public static String splitToLastParts(String string, String pattern, int partCount) {
        String[] parts = string.split(pattern);
        if (parts.length < partCount) return string;

        String[] lastParts = Arrays.copyOfRange(parts, parts.length - partCount, parts.length);
        return String.join(".", lastParts);
    }

    public static String splitToLastKey(String string, String keyToMatches, String pattern) {
        String[] parts = string.split(pattern);

        int keyIndex = Arrays.asList(parts).indexOf(keyToMatches);
        if (keyIndex == -1) return string;

        String[] remainingParts = Arrays.copyOfRange(parts, keyIndex, parts.length);
        return String.join(".", remainingParts);
    }
}
