package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.me.service.helpers.NetworkCraftingProviders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(NetworkCraftingProviders.class)
public interface AccessorNetworkProviders {
    @Mixin(targets = "appeng.me.service.helpers.NetworkCraftingProviders$CraftingProviderList")
    interface AccessorProviderList {
        @Accessor("providers")
        List<ICraftingProvider> getProviders();
    }
}
