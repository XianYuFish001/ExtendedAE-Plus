package com.extendedae_plus.mixin.extension;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.GenericStack;
import appeng.me.service.CraftingService;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.impl.pattern.smartDoubling.RequestedAmountHolder;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorNetworkProviders;
import com.google.common.math.LongMath;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface IScaledPattern extends IPatternDetails {
    long eaep$multiplier();

    boolean eaep$enabled();

    void eaep$setEnabled(boolean value);

    List<GenericStack> eaep$getInputs();

    List<GenericStack> eaep$getOutputs();

    /// @param multiplier Negative: Divide
    IPatternDetails eaep$create(long multiplier, boolean saveInfo);

    default IPatternDetails eaep$create(long multiplier) {
        return eaep$create(multiplier, true);
    }

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
                serviceCrafting.getProviders(this))
                .getProviders()
                .size();
        if (sizeProviders < 2)
            return this.eaep$create(amountRequested);

        amountRequested = Math.ceilDiv(amountRequested, sizeProviders);
        if (amountRequested < 1) return null;
        return this.eaep$create(amountRequested);
    }

    static List<GenericStack> process(List<GenericStack> target, long multiplier) throws ArithmeticException {
        var multiplied = new ArrayList<GenericStack>();
        target.forEach(stack -> {
            if (stack == null) {
                multiplied.add(null);
                return;
            }

            long amountMultiplied;
            try {
                amountMultiplied = multiplier >= 0
                        ? LongMath.saturatedMultiply(stack.amount(), multiplier)
                        : LongMath.divide(stack.amount(), -multiplier, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException exception) {
                return;
            }
            if (amountMultiplied == 0) return;
            multiplied.add(new GenericStack(stack.what(), amountMultiplied));
        });
        if (multiplied.size() != target.size())
            throw new ArithmeticException("Failed to divide pattern");
        return Collections.unmodifiableList(multiplied);
    }
}
