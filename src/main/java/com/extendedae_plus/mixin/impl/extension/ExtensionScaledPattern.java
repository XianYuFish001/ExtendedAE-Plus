package com.extendedae_plus.mixin.impl.extension;

import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.me.service.CraftingService;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.impl.pattern.smartDoubling.RequestedAmountHolder;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorNetworkProviders;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public interface ExtensionScaledPattern {
    default IPatternDetails eaep$instance() {
        return (IPatternDetails) this;
    }

    long eaep$multiplier();

    boolean eaep$enabled();

    void eaep$setEnabled(boolean value);

    IPatternDetails eaep$create(long multiplier);

    default @Nullable IPatternDetails eaep$create(ICraftingService iServiceCrafting) {
        var amountRequested = RequestedAmountHolder.get();
        RequestedAmountHolder.pop();
        if (amountRequested < 1) return null;

        int multiplierMax = EAEPConfig.smartDoublingMaxMultiplier.get();
        if (multiplierMax > 0)
            amountRequested = Math.min(multiplierMax, amountRequested);

        if (!EAEPConfig.providerRoundRobin.get())
            return this.eaep$create(amountRequested);

        if (!(iServiceCrafting instanceof CraftingService serviceCrafting))
            return null;

        var sizeProviders = ((AccessorNetworkProviders.AccessorProviderList)
                serviceCrafting.getProviders(this.eaep$instance()))
                .getProviders()
                .size();
        if (sizeProviders < 2)
            return this.eaep$create(amountRequested);

        amountRequested = Math.ceilDiv(amountRequested, sizeProviders);
        if (amountRequested < 1) return null;
        return this.eaep$create(amountRequested);
    }

    static Optional<ExtensionScaledPattern> of(@Nullable Object original) {
        if (!(original instanceof ExtensionScaledPattern extension) || !extension.eaep$enabled())
            return Optional.empty();
        return Optional.of(extension);
    }

    static Consumer<IPatternDetails> setState(YesNo enabled) {
        var stateBoolean = enabled == YesNo.YES;
        return pattern -> {
            if (!(pattern instanceof ExtensionScaledPattern extension)) return;
            extension.eaep$setEnabled(stateBoolean);
        };
    }
}
