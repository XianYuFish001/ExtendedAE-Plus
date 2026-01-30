package com.extendedae_plus.mixin.extension;

import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.me.service.CraftingService;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.impl.pattern.smartDoubling.RequestedAmountHolder;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorNetworkProviders;
import com.google.common.math.LongMath;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.advanced_ae.common.patterns.AdvProcessingPattern;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface ExtensionScaledPattern {
    default IPatternDetails eaep$instance() {
        return (IPatternDetails) this;
    }

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
                serviceCrafting.getProviders(this.eaep$instance()))
                .getProviders()
                .size();
        if (sizeProviders < 2)
            return this.eaep$create(amountRequested);

        amountRequested = Math.ceilDiv(amountRequested, sizeProviders);
        if (amountRequested < 1) return null;
        return this.eaep$create(amountRequested);
    }

    static List<GenericStack> process(List<GenericStack> target, long multiplier) {
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
        if (multiplied.size() != target.size()) return target;
        return Collections.unmodifiableList(multiplied);
    }

    static void writeToStack(ItemStack stack, IPatternDetails pattern) {
        of(pattern, true).ifPresent(extension -> {
            if (extension instanceof AdvProcessingPattern patternAdv)
                    AdvProcessingPattern.encode(stack,
                            patternAdv.getSparseInputs(),
                            patternAdv.getSparseOutputs(),
                            patternAdv.getDirectionMap());
            else AEProcessingPattern.encode(stack,
                    extension.eaep$getInputs(),
                    extension.eaep$getOutputs());
        });
    }

    static Optional<ExtensionScaledPattern> of(@Nullable Object original, boolean containsUnavailable) {
        if (!(original instanceof ExtensionScaledPattern extension) || !(containsUnavailable || extension.eaep$enabled()))
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
