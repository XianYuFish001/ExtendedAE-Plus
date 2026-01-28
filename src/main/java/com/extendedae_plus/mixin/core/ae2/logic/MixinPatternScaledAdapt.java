package com.extendedae_plus.mixin.core.ae2.logic;

import appeng.helpers.patternprovider.PatternProviderLogic;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 自适应调整样板大小 感觉性能影响比较大
 * 测试中好像本来也不会堵, 难道是最近修了?
 * (未完成)
 */
@Mixin(PatternProviderLogic.class)
public abstract class MixinPatternScaledAdapt {
//    @Shadow
//    protected abstract Set<Direction> getActiveSides();
//    @Shadow
//    protected abstract @Nullable PatternProviderTarget findAdapter(Direction side);
//
//    @Shadow
//    @Final
//    private List<GenericStack> sendList;
//
//    @ModifyVariable(method = "pushPattern", at = @At("HEAD"), argsOnly = true)
//    private IPatternDetails adaptSize(IPatternDetails original, IPatternDetails a, KeyCounter[] inputHolder) {
//        if (!EAEPConfig.smartDoublingAdapt.get()) return original;
//        if (!original.supportsPushInputsToExternalInventory()) return original;
//
//        var extension = ExtensionScaledPattern.of(original);
//        if (extension.map(ExtensionScaledPattern::eaep$multiplier).orElse(0L) <= 0)
//            return original;
//
//        var base = extension.get().eaep$create(1 / extension.get().eaep$multiplier());
//        if (base == null) return original;
//
//        long multiplierMax = 0;
//        for (var side : this.getActiveSides()) {
//            var target = this.findAdapter(side);
//            if (target == null) continue;
//
//            var limit = Long.MAX_VALUE;
//            for (var inputs : base.getInputs()) {
//                var inserted = target.insert(inputs.getPossibleInputs()[0].what(),
//                        inputs.getMultiplier(),
//                        Actionable.SIMULATE);
//                limit = Math.min(limit, inserted / inputs.getMultiplier());
//            }
//            multiplierMax = Math.max(multiplierMax, limit);
//        }
//        if (multiplierMax == 0) return original;
//        if (multiplierMax >= extension.get().eaep$multiplier()) return original;
//
//        var adapted = extension.get().eaep$create(multiplierMax);
//        if (adapted == null) return original;
//        return adapted;
//    }
//
//    @Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
//    private boolean allowScaled(List<IPatternDetails> instance, Object object) {
//        if (!EAEPConfig.smartDoublingAdapt.get()) return instance.contains(object);
//
//        var extension = ExtensionScaledPattern.of(object);
//        if (extension.map(ExtensionScaledPattern::eaep$multiplier).orElse(0L) <= 0)
//            return instance.contains(object);
//        return true;
//    }
}
