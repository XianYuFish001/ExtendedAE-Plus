package com.extendedae_plus.mixin.core.ae2.smartDoubling;

import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.inv.CraftingSimulationState;
import com.extendedae_plus.common.impl.pattern.smartDoubling.HolderCraftingAmount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingTreeNode.class,remap = false)
public class CraftingTreeNodeMixin {
    @Inject(method = "request(Lappeng/crafting/inv/CraftingSimulationState;JLappeng/api/stacks/KeyCounter;)V",
            at = @At(value = "INVOKE",
                    target = "Lappeng/crafting/CraftingTreeNode;addContainerItems(Lappeng/api/stacks/AEKey;JLappeng/api/stacks/KeyCounter;)V"))
    private void captureRequestedAmount(CraftingSimulationState inv, long requestedAmount, KeyCounter containerItems, CallbackInfo ci) {
        // push the requestedAmount before addContainerItems is called
       HolderCraftingAmount.push(requestedAmount);
    }
}
