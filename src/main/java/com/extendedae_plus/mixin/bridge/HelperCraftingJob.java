package com.extendedae_plus.mixin.bridge;

import appeng.api.crafting.IPatternDetails;

import java.util.Map;

public interface HelperCraftingJob {
    Map<IPatternDetails, HelperJobProgress> getTasks();

    interface HelperJobProgress {
        long getValue();
    }
}
