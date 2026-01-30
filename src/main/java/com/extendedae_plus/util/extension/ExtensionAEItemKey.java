package com.extendedae_plus.util.extension;

import appeng.api.stacks.AEItemKey;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorItemKey;
import net.minecraft.core.component.DataComponentType;

import java.util.function.UnaryOperator;

public class ExtensionAEItemKey {
    public static <TType> AEItemKey set(AEItemKey instance, DataComponentType<TType> type, TType value) {
        var stack = instance.toStack();
        stack.set(type, value);
        return AccessorItemKey.eaep$newInstance(stack);
    }

    public static <TType> AEItemKey update(AEItemKey instance,
                                           DataComponentType<TType> type,
                                           TType valueDefault,
                                           UnaryOperator<TType> updater) {
        var stack = instance.toStack();
        var value = stack.getOrDefault(type, valueDefault);
        stack.set(type, updater.apply(value));
        return AccessorItemKey.eaep$newInstance(stack);
    }
}
