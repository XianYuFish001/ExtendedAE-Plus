package com.extendedae_plus.mixin.core.ae2;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.fish.fishlib.mixin.MixinDependencies;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@MixinDependencies(conflict = "expandedae")
@Mixin(value = CraftingCPUCluster.class, priority = 1100)
public abstract class MixinClusterCPU {
    @ModifyConstant(
        method = "addBlockEntity(Lappeng/blockentity/crafting/CraftingBlockEntity;)V",
        constant = @Constant(intValue = 16)
    )
    private int unlimit(int original) {
        return Integer.MAX_VALUE;
    }
}
