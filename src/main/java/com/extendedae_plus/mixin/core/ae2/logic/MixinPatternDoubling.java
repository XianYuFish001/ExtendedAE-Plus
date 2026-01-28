package com.extendedae_plus.mixin.core.ae2.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEProcessingPattern;
import com.extendedae_plus.mixin.impl.extension.ExtensionScaledPattern;
import com.google.common.math.LongMath;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@Mixin(AEProcessingPattern.class)
public class MixinPatternDoubling implements ExtensionScaledPattern {
    @Unique
    private static Constructor<IPatternDetails.IInput> eaep$constructorInput;

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
    @Mutable
    private List<GenericStack> condensedOutputs;
    @Shadow
    @Final
    private AEItemKey definition;

    /// 我真没招了 改类型不行反射不行at用不了 现在知道为什么之前不直接mixin这里了😓😓😓
    @Unique
    private IPatternDetails.IInput[] eaep$inputs;
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

    /// @param multiplier Ignore config "smartDoublingMaxMultiplier"
    @Unique
    @Override
    @SuppressWarnings("unchecked")
    public IPatternDetails eaep$create(long multiplier) {
        var multiplied = this.getClass().cast(new AEProcessingPattern(this.definition));
        multiplied.eaep$setEnabled(true);
        multiplied.eaep$multiplier = multiplier;

        var cacheMultiplied = new ArrayList<GenericStack>();

        final Consumer<GenericStack> mapperStack = stack -> {
            if (stack == null) cacheMultiplied.add(null);
            else cacheMultiplied.add(new GenericStack(stack.what(),
                    LongMath.saturatedMultiply(stack.amount(), multiplier)));
        };

        this.sparseInputs.forEach(mapperStack);
        // List#copyOf 无法接受nullable元素
        multiplied.sparseInputs = Collections.unmodifiableList(new ArrayList<>(cacheMultiplied));
        cacheMultiplied.clear();

        this.sparseOutputs.forEach(mapperStack);
        multiplied.sparseOutputs = Collections.unmodifiableList(new ArrayList<>(cacheMultiplied));
        cacheMultiplied.clear();

        /// @see appeng.crafting.pattern.AEPatternHelper#condenseStacks(List)
        final UnaryOperator<List<GenericStack>> condenser = sparseInput -> {
            var map = new LinkedHashMap<AEKey, Long>();

            sparseInput.stream()
                    .filter(Objects::nonNull)
                    .forEach(stack -> map.merge(stack.what(), stack.amount(), LongMath::saturatedAdd));

            if (map.isEmpty()) throw new IllegalStateException("No pattern here!");

            return map.entrySet().stream()
                    .map(entry -> new GenericStack(entry.getKey(), entry.getValue()))
                    .toList();
        };

        multiplied.eaep$inputs = condenser.apply(multiplied.sparseInputs).stream().map(stack -> {
            try {
                if (eaep$constructorInput == null) {
                    var clazzInput = Class.forName("appeng.crafting.pattern.AEProcessingPattern$Input");
                    eaep$constructorInput = (Constructor<IPatternDetails.IInput>) clazzInput.getDeclaredConstructor(GenericStack.class);
                }
                return eaep$constructorInput.newInstance(stack);
            } catch (NoSuchMethodException
                     | InstantiationException
                     | IllegalAccessException
                     | InvocationTargetException
                     | ClassNotFoundException exception) {
                return null;
            }
        }).toArray(IPatternDetails.IInput[]::new);

        multiplied.condensedOutputs.clear();
        multiplied.condensedOutputs = condenser.apply(multiplied.sparseOutputs);
        return multiplied.eaep$instance();
    }

    @Inject(method = "getInputs", at = @At("HEAD"), cancellable = true)
    private void getInputs(CallbackInfoReturnable<IPatternDetails.IInput[]> cir) {
        if (this.eaep$inputs == null) return;
        cir.setReturnValue(this.eaep$inputs);
    }

    @Inject(method = "pushInputsToExternalInventory", at = @At("HEAD"), cancellable = true)
    private void onPush(KeyCounter[] inputHolder, IPatternDetails.PatternInputSink inputSink, CallbackInfo ci) {
        if (this.eaep$inputs == null) return;
        if (this.sparseInputs.size() != this.eaep$inputs.length) return;
        this.eaep$instance().pushInputsToExternalInventory(inputHolder, inputSink);
        ci.cancel();
    }
}
