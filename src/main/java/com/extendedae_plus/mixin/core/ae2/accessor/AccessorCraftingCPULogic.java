package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingCpuLogic.class)
public interface AccessorCraftingCPULogic {
    @Accessor("job")
    ExecutingCraftingJob getJob();
}
