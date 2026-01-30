package com.extendedae_plus.mixin.bridge;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.helpers.patternprovider.PatternContainer;

import java.util.List;
import java.util.Map;

public interface BridgeProviderList {
    Map<PatternContainerGroup, List<PatternContainer>> eaep$getProviderList();
}
