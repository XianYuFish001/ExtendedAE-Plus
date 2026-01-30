package com.extendedae_plus.mixin.extension;

import appeng.api.stacks.AEKey;

import java.util.Optional;

public interface IAEItemKey {
    static Optional<IAEItemKey> of(AEKey key) {
        if (!(key instanceof IAEItemKey extension))
            return Optional.empty();
        return Optional.of(extension);
    }
}
