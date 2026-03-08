package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.api.networking.IManagedGridNode;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.extendedae_plus.mixin.helper.HelperProviderHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatternProviderLogic.class)
public interface AccessorProviderLogic extends HelperProviderHost {
    @Accessor("host")
    PatternProviderLogicHost getHostVanilla();

    @Accessor("mainNode")
    IManagedGridNode getGridNode();
}
