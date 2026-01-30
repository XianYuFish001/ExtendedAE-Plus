package com.extendedae_plus.mixin.extension;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorItemKey;
import net.minecraft.core.component.DataComponentType;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface ExtensionAEItemKey {
    static <TType> UnaryOperator<AEItemKey> set(DataComponentType<TType> type, TType value) {
        return key -> {
            var stack = key.toStack();
            stack.set(type, value);
            return AccessorItemKey.eaep$newInstance(stack);
        };
    }

    static <TType> UnaryOperator<AEItemKey> update(DataComponentType<TType> type,
                                                   TType valueDefault,
                                                   UnaryOperator<TType> updater) {
        return key -> {
            var stack = key.toStack();
            var value = stack.getOrDefault(type, valueDefault);
            stack.set(type, updater.apply(value));
            return AccessorItemKey.eaep$newInstance(stack);
        };
    }

    static Optional<ExtensionAEItemKey> of(AEKey key) {
        if (!(key instanceof ExtensionAEItemKey extension))
            return Optional.empty();
        return Optional.of(extension);
    }
}
