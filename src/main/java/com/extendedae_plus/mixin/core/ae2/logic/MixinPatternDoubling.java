package com.extendedae_plus.mixin.core.ae2.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedProcessingPattern;
import com.extendedae_plus.mixin.extension.IScaledPattern;
import com.extendedae_plus.util.extension.ExtensionMiscKt;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(AEProcessingPattern.class)
public abstract class MixinPatternDoubling implements IScaledPattern {
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
        List<GenericStack> inputs;
        List<GenericStack> outputs;
        try {
            inputs = IScaledPattern.process(this.sparseInputs, multiplier);
            outputs = IScaledPattern.process(this.sparseOutputs, multiplier);
        } catch (ArithmeticException exception) {
            inputs = this.sparseInputs;
            outputs = this.sparseOutputs;
        }

        var multiplied = this.getClass().cast(new AEProcessingPattern(
                ExtensionMiscKt.set(this.definition, AEComponents.ENCODED_PROCESSING_PATTERN,
                                new EncodedProcessingPattern(inputs, outputs))));
        if (saveInfo) {
            multiplied.eaep$setEnabled(true);
            multiplied.eaep$multiplier = multiplier;
        }
        return multiplied;
    }
}
