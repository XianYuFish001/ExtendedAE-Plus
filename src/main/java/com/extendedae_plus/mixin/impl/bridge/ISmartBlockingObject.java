package com.extendedae_plus.mixin.impl.bridge;

public interface ISmartBlockingObject {
    boolean eaep$getBlockingState();
    void eaep$setBlockingState(boolean value);

    boolean eaep$isBlockingDisabled();
    void eaep$disableBlocking();
}
