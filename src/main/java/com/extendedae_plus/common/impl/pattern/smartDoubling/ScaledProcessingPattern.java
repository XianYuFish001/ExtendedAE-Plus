package com.extendedae_plus.common.impl.pattern.smartDoubling;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEProcessingPattern;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 缩放后的处理样板，结构完全模拟 AEProcessingPattern。
 * 保持 sparse/condensed/inputs 的一致性，同时保存原始样板。
 *
 * @param original         原始样板引用
 * @param definition       样板物品
 * @param sparseInputs     缩放后的稀疏输入（List 以适配 1.21 API）
 * @param sparseOutputs    缩放后的稀疏输出（List 以适配 1.21 API）
 * @param inputs           缩放后的压缩输入
 * @param condensedOutputs 缩放后的压缩输出（List 以适配 1.21 API）
 */
public record ScaledProcessingPattern(AEProcessingPattern original, AEItemKey definition,
                                      List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs,
                                      IInput[] inputs, List<GenericStack> condensedOutputs) implements IPatternDetails {
    public ScaledProcessingPattern(
            AEProcessingPattern original,
            AEItemKey definition,
            List<GenericStack> sparseInputs,
            List<GenericStack> sparseOutputs,
            IInput[] inputs,
            List<GenericStack> condensedOutputs
    ) {
        this.original = Objects.requireNonNull(original, "original");
        this.definition = Objects.requireNonNull(definition, "def");
        this.sparseInputs = List.copyOf(Objects.requireNonNull(sparseInputs, "sparseInputs"));
        this.sparseOutputs = List.copyOf(Objects.requireNonNull(sparseOutputs, "sparseOutputs"));
        this.inputs = Objects.requireNonNull(inputs, "inputs");
        this.condensedOutputs = List.copyOf(Objects.requireNonNull(condensedOutputs, "condensedOutputs"));
    }

    /* -------------------- API 实现 -------------------- */

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return condensedOutputs;
    }

    @Override
    public GenericStack getPrimaryOutput() {
        if (!condensedOutputs.isEmpty()) return condensedOutputs.getFirst();
        return original.getPrimaryOutput();
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return original.supportsPushInputsToExternalInventory();
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        // 保持和 AEProcessingPattern 一致，用 sparseInputs 驱动
        if (sparseInputs.size() == inputs.length) {
            IPatternDetails.super.pushInputsToExternalInventory(inputHolder, inputSink);
        } else {
            KeyCounter allInputs = new KeyCounter();
            for (KeyCounter counter : inputHolder) {
                allInputs.addAll(counter);
            }
            for (GenericStack sparseInput : sparseInputs) {
                if (sparseInput != null) {
                    AEKey key = sparseInput.what();
                    long amount = sparseInput.amount();
                    long available = allInputs.get(key);
                    if (available < amount) {
                        throw new RuntimeException("Expected at least %d of %s when pushing scaled pattern, but only %d available"
                                .formatted(amount, key, available));
                    }
                    inputSink.pushInput(key, amount);
                    allInputs.remove(key, amount);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ScaledProcessingPattern{" +
                ("\n    original={" +
                        "\n        inputs=" + Arrays.toString(original.getInputs()) +
                        "\n        sparseInputs=" + original.getSparseInputs() +
                        "\n        sparseOutputs=" + original.getSparseOutputs() +
                "   }") +
                "\n    definition=" + definition +
                "\n    sparseInputs=" + sparseInputs +
                "\n    sparseOutputs=" + sparseOutputs +
                "\n    inputs=" + Arrays.toString(inputs) +
                "\n    condensedOutputs=" + condensedOutputs +
                '}';
    }

    /* -------------------- 缩放输入代理 -------------------- */

    public record Input(GenericStack[] template, long multiplier) implements IInput {
        public GenericStack[] getPossibleInputs() {
            return this.template;
        }

        public long getMultiplier() {
            return this.multiplier;
        }

        public boolean isValid(AEKey input, Level level) {
            return input.matches(this.template[0]);
        }

        public @Nullable AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
