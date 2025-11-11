package com.extendedae_plus.common.impl.pattern.smartDoubling;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import com.extendedae_plus.EAEPConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public final class PatternScaler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ScaledProcessingPattern scale(AEProcessingPattern base, AEKey target, long requestedAmount) {
        if (base == null) throw new IllegalArgumentException("base");
        if (target == null) throw new IllegalArgumentException("target");

        // 双保险：若样板标记为不允许缩放，直接放弃缩放（返回 null 表示调用方应保持原样板）
        if (base instanceof SmartDoublingAwarePattern aware && !aware.eap$allowScaling()) {
            return null;
        }

        List<GenericStack> baseSparseInputs = base.getSparseInputs();
        List<GenericStack> baseSparseOutputs = base.getSparseOutputs();
        IInput[] baseInputs = base.getInputs();
        List<GenericStack> baseOutputs = base.getOutputs();

        // 新逻辑：不再对样板进行单位化处理
        // 找到目标输出在 outputs 中的索引（尝试匹配 target，否则取第一个非空输出）
        int targetOutIndex = -1;
        for (int i = 0; i < baseOutputs.size(); i++) {
            var out = baseOutputs.get(i);
            if (out != null && out.what() != null && out.what().equals(target)) {
                targetOutIndex = i;
                break;
            }
        }
        if (targetOutIndex == -1) {
            for (int i = 0; i < baseOutputs.size(); i++) {
                if (baseOutputs.get(i) != null) {
                    targetOutIndex = i;
                    break;
                }
            }
        }
        if (targetOutIndex == -1 && !baseOutputs.isEmpty()) targetOutIndex = 0;

        long perOperationTarget = 1L;
        if (targetOutIndex >= 0 && baseOutputs.get(targetOutIndex) != null) {
            long amt = baseOutputs.get(targetOutIndex).amount();
            if (amt > 0) perOperationTarget = amt;
        }

        // 使用最小整数倍（ceil）策略：直接选择满足请求的最小倍数
        long multiplier = 1L;
        if (requestedAmount > 0) {
            long needed = requestedAmount / perOperationTarget + ((requestedAmount % perOperationTarget) == 0 ? 0 : 1);
            multiplier = Math.max(needed, 1L);
        }
        // 应用配置的最大倍数上限（0 表示不限制）
        try {
            int maxMul = EAEPConfig.SMART_SCALING_MAX_MULTIPLIER.get();
            if (maxMul > 0 && multiplier > maxMul) {
                multiplier = maxMul;
            }
        } catch (Throwable ignore) {
            // 配置读取异常时不施加上限
        }

        // 构建压缩输入（将每个输入的 multiplier 翻倍，保留每个模板的原始数量）
        IInput[] scaledInputs = new IInput[baseInputs.length];
        for (int i = 0; i < baseInputs.length; i++) {
            var in = baseInputs[i];
            var template = in.getPossibleInputs();
            GenericStack[] scaledTemplates = new GenericStack[template.length];
            for (int j = 0; j < template.length; j++) {
                scaledTemplates[j] = new GenericStack(template[j].what(), template[j].amount());
            }
            scaledInputs[i] = new ScaledProcessingPattern.Input(scaledTemplates, in.getMultiplier() * multiplier);
        }

        // 构建压缩输出（List）
        var finalMul = multiplier;
        var scaledCondensedOutputs = baseOutputs.stream()
                .filter(Objects::nonNull)
                .map(value -> new GenericStack(value.what(), value.amount() * finalMul))
                .toList();
        var scaledSparseInputs = baseSparseInputs.stream()
                .filter(Objects::nonNull)
                .map(value -> new GenericStack(value.what(), value.amount() * finalMul))
                .toList();
        var scaledSparseOutputs = baseSparseOutputs.stream()
                .filter(Objects::nonNull)
                .map(value -> new GenericStack(value.what(), value.amount() * finalMul))
                .toList();

        var scaled = new ScaledProcessingPattern(base,
                base.getDefinition(),
                scaledSparseInputs,
                scaledSparseOutputs,
                scaledInputs,
                scaledCondensedOutputs);
        LOGGER.debug("[EAEP/debug] 倍增样板构建结果: {}", scaled);
        return scaled;
    }
}
