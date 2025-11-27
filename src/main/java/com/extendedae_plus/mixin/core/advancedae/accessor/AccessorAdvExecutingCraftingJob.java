package com.extendedae_plus.mixin.core.advancedae.accessor;

import appeng.api.crafting.IPatternDetails;
import com.extendedae_plus.mixin.impl.bridge.HelperCraftingJob;
import net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ExecutingCraftingJob.class)
public interface AccessorAdvExecutingCraftingJob extends HelperCraftingJob {
    @Accessor("tasks")
    Map<IPatternDetails, HelperJobProgress> getTasks();

    @Mixin(targets = "net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob$TaskProgress")
    interface AccessorAdvTaskProgress extends HelperJobProgress {
        @Accessor("value")
        long getValue();
    }
}
