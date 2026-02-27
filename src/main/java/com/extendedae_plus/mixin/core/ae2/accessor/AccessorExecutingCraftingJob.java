package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.ExecutingCraftingJob;
import com.extendedae_plus.mixin.helper.HelperCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ExecutingCraftingJob.class)
public interface AccessorExecutingCraftingJob extends HelperCraftingJob {
    @Override
    @Accessor("tasks")
    Map<IPatternDetails, HelperJobProgress> getTasks();

    @Override
    @Accessor("link")
    CraftingLink getLink();

    @Override
    @Accessor("finalOutput")
    GenericStack getOutputFinal();

    @Override
    @Accessor("playerId")
    Integer getPlayerID();

    @Override
    @Accessor("remainingAmount")
    long getRemainingAmount();

    @Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress")
    interface AccessorTaskProgress extends HelperJobProgress {
        @Accessor("value")
        long getValue();
    }
}
