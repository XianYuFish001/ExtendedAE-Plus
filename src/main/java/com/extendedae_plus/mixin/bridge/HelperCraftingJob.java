package com.extendedae_plus.mixin.bridge;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;

import java.util.Map;

public interface HelperCraftingJob {
    Map<IPatternDetails, HelperJobProgress> getTasks();

    CraftingLink getLink();

    GenericStack getOutputFinal();

    Integer getPlayerID();

    long getRemainingAmount();

    interface HelperJobProgress {
        long getValue();
    }
}
