package com.extendedae_plus.util.extension;

import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.crafting.pattern.AEProcessingPattern;
import com.extendedae_plus.mixin.extension.IScaledPattern;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.advanced_ae.common.patterns.AdvProcessingPattern;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class ExtensionScaledPattern {
    public static Optional<IScaledPattern> of(@Nullable Object original, boolean containsUnavailable) {
        if (!(original instanceof IScaledPattern extension) || !(containsUnavailable || extension.eaep$enabled()))
            return Optional.empty();
        return Optional.of(extension);
    }

    public static void writeToStack(IPatternDetails instance, ItemStack stack) {
        if (instance instanceof AdvProcessingPattern patternAdv)
            AdvProcessingPattern.encode(stack,
                    patternAdv.getSparseInputs(),
                    patternAdv.getSparseOutputs(),
                    patternAdv.getDirectionMap());
        else if (instance instanceof IScaledPattern extension)
            AEProcessingPattern.encode(stack,
                extension.eaep$getInputs(),
                extension.eaep$getOutputs());
    }

    public static IPatternDetails original(IPatternDetails instance) {
        return of(instance, true)
                .map(IScaledPattern::eaep$original)
                .orElse(instance);
    }

    public static IPatternDetails create(IPatternDetails instance, ICraftingService serviceCrafting) {
        return of(instance, false)
                .map(extension -> extension.eaep$create(serviceCrafting))
                .orElse(instance);
    }

    public static IPatternDetails create(IPatternDetails instance, long multiplier, boolean saveInfo) {
        return of(instance, true)
                .map(extension -> extension.eaep$create(multiplier, saveInfo))
                .orElse(instance);
    }

    public static IPatternDetails create(IPatternDetails instance, long multiplier) {
        return create(instance, multiplier, true);
    }

    public static Consumer<IPatternDetails> setState(YesNo enabled) {
        var stateBoolean = enabled == YesNo.YES;
        return pattern -> {
            if (!(pattern instanceof IScaledPattern extension)) return;
            extension.eaep$setEnabled(stateBoolean);
        };
    }
}
