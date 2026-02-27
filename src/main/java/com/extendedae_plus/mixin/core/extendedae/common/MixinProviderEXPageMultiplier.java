package com.extendedae_plus.mixin.core.extendedae.common;

import com.extendedae_plus.EAEPConfig;
import com.glodblock.github.extendedae.common.parts.PartExPatternProvider;
import com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = {TileExPatternProvider.class, PartExPatternProvider.class}, priority = 1100, remap = false)
public abstract class MixinProviderEXPageMultiplier {
    @ModifyArg(
            method = "createLogic",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V"
            ),
            index = 2
    )
    private int modifyCapacity(int original) {
        return Math.max(1, original) * EAEPConfig.INSTANCE.getEXProviderPageMultiplier();
    }
}
