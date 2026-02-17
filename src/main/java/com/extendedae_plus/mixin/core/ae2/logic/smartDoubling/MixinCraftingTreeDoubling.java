package com.extendedae_plus.mixin.core.ae2.logic.smartDoubling;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.CraftingTreeProcess;
import com.extendedae_plus.util.extension.ExtensionScaledPattern;
import lombok.experimental.ExtensionMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftingTreeProcess.class)
@ExtensionMethod(ExtensionScaledPattern.class)
public abstract class MixinCraftingTreeDoubling {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, name = "arg3")
    private static IPatternDetails scale(IPatternDetails original,
                                         ICraftingService serviceCrafting,
                                         CraftingCalculation job,
                                         IPatternDetails details,
                                         CraftingTreeNode craftingTreeNode) {
        return original.create(serviceCrafting);
    }
}
