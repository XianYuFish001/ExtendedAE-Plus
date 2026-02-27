package com.extendedae_plus.mixin.core.advancedae.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorItemKey;
import com.extendedae_plus.mixin.extension.IScaledPattern;
import net.minecraft.core.Direction;
import net.pedroksl.advanced_ae.common.patterns.AdvProcessingPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.LinkedHashMap;
import java.util.List;

@Mixin(AdvProcessingPattern.class)
public abstract class MixinAdvPatternDoubling implements IScaledPattern {
    @Shadow
    @Final
    private List<GenericStack> sparseInputs;
    @Shadow
    @Final
    private List<GenericStack> sparseOutputs;
    @Shadow
    @Final
    private AEItemKey definition;
    @Shadow
    @Final
    private LinkedHashMap<AEKey, Direction> dirMap;

    @Unique
    private boolean eaep$enabled;
    @Unique
    private long eaep$multiplier;

    @Override
    public long getEaep$multiplier() {
        return this.eaep$multiplier;
    }

    @Override
    public boolean getEaep$enabled() {
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

        var stackPattern = this.definition.toStack();
        AdvProcessingPattern.encode(stackPattern, inputs, outputs, this.dirMap);
        var multiplied = this.getClass().cast(new AdvProcessingPattern(
                AccessorItemKey.eaep$newInstance(stackPattern)));
        if (saveInfo) {
            multiplied.eaep$setEnabled(true);
            multiplied.eaep$multiplier = multiplier;
        }
        return multiplied;
    }
}
