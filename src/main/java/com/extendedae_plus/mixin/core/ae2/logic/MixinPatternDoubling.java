package com.extendedae_plus.mixin.core.ae2.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedProcessingPattern;
import com.extendedae_plus.mixin.impl.extension.ExtensionAEItemKey;
import com.extendedae_plus.mixin.impl.extension.ExtensionScaledPattern;
import com.google.common.math.LongMath;
import org.spongepowered.asm.mixin.*;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Mixin(AEProcessingPattern.class)
public class MixinPatternDoubling implements ExtensionScaledPattern {
    @Shadow
    @Final
    @Mutable
    private List<GenericStack> sparseInputs;
    @Shadow
    @Final
    @Mutable
    private List<GenericStack> sparseOutputs;
    @Shadow
    @Final
    private AEItemKey definition;

    @Unique
    private boolean eaep$enabled;
    @Unique
    private long eaep$multiplier;

    @Override
    public long eaep$multiplier() {
        return this.eaep$multiplier;
    }

    @Override
    public boolean eaep$enabled() {
        return this.eaep$enabled;
    }

    @Override
    public void eaep$setEnabled(boolean value) {
        this.eaep$enabled = value;
    }

    @Override
    public List<GenericStack> eaep$getInputs() {
        return this.sparseInputs;
    }

    @Override
    public List<GenericStack> eaep$getOutputs() {
        return this.sparseOutputs;
    }

    /**
     * 我傻了 原来不用操作condensed(
     * @param multiplier Negative: Divide.
     *                   Ignore config "smartDoublingMaxMultiplier"
     * @param saveInfo   Save the multiplied info {@linkplain #eaep$multiplier }, {@linkplain #eaep$enabled}
     */
    @Unique
    @Override
    public IPatternDetails eaep$create(long multiplier, boolean saveInfo) {
        var cacheMultiplied = new ArrayList<GenericStack>();
        final Consumer<GenericStack> mapperStack = stack -> {
            if (stack == null) {
                cacheMultiplied.add(null);
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
            cacheMultiplied.add(new GenericStack(stack.what(), amountMultiplied));
        };

        this.sparseInputs.forEach(mapperStack);
        if (cacheMultiplied.size() != this.sparseInputs.size()) return this.eaep$instance();
        // List#copyOf 无法接受nullable元素
        var inputs = Collections.unmodifiableList(new ArrayList<>(cacheMultiplied));
        cacheMultiplied.clear();

        this.sparseOutputs.forEach(mapperStack);
        if (cacheMultiplied.size() != this.sparseOutputs.size()) return this.eaep$instance();
        var outputs = Collections.unmodifiableList(new ArrayList<>(cacheMultiplied));
        cacheMultiplied.clear();

        var multiplied = this.getClass().cast(new AEProcessingPattern(
                ExtensionAEItemKey.set(AEComponents.ENCODED_PROCESSING_PATTERN,
                                new EncodedProcessingPattern(inputs, outputs))
                        .apply(this.definition)));
        if (saveInfo) {
            multiplied.eaep$setEnabled(true);
            multiplied.eaep$multiplier = multiplier;
        }
        return multiplied.eaep$instance();
    }
}
