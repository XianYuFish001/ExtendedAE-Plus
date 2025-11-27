package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.execution.ExecutingCraftingJob;
import com.extendedae_plus.mixin.impl.bridge.HelperCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ExecutingCraftingJob.class)
public interface AccessorExecutingCraftingJob extends HelperCraftingJob {
    @Accessor("tasks")
    Map<IPatternDetails, HelperJobProgress> getTasks();

    @Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress")
    interface AccessorTaskProgress extends HelperJobProgress {
        @Accessor("value")
        long getValue();
    }
}
