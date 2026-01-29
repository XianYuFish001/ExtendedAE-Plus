package com.extendedae_plus.mixin.impl;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;

@FunctionalInterface
public interface IOerMEStorage {
    long apply(AEKey what, long amount, Actionable mode, IActionSource source);
}
