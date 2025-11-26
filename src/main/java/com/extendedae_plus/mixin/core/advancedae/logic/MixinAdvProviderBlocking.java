package com.extendedae_plus.mixin.core.advancedae.logic;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderTarget;
import com.extendedae_plus.mixin.impl.bridge.ISmartBlockingObject;
import net.minecraft.nbt.CompoundTag;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(value = AdvPatternProviderLogic.class, remap = false)
public class MixinAdvProviderBlocking implements ISmartBlockingObject {
    @Shadow
    @Final
    private IConfigManager configManager;
    @Unique
    private static final String COMPOUND_KEY_BLOCKING = "eaep_blocking";
    @Unique
    private static final String COMPOUND_KEY_BLOCKING_DISABLED = "eaep_blocking_disabled";

    @Unique
    private boolean eaep$smartBlocking = false;
    @Unique
    private boolean eaep$blockingDisabled = false;

    @Override
    public boolean eaep$getBlockingState() {
        return !this.eaep$blockingDisabled && this.eaep$smartBlocking;
    }

    @Override
    public void eaep$setBlockingState(boolean value) {
        this.eaep$smartBlocking = value;
        this.eaep$blockingDisabled = false;
    }

    @Override
    public boolean eaep$isBlockingDisabled() {
        return this.eaep$blockingDisabled;
    }

    @Override
    public void eaep$disableBlocking() {
        this.eaep$blockingDisabled = true;
    }

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;I)V",
            at = @At("TAIL"))
    private void onInit(IManagedGridNode mainNode, AdvPatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        if (!this.configManager.hasSetting(Settings.BLOCKING_MODE)) return;

        var setting = this.configManager.getSetting(Settings.BLOCKING_MODE);
        this.eaep$setBlockingState(setting.equals(YesNo.YES));
        if (setting.equals(YesNo.NO))
            this.eaep$disableBlocking();
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void eap$writeAdvancedToNbt(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putBoolean(COMPOUND_KEY_BLOCKING, this.eaep$smartBlocking);
        tag.putBoolean(COMPOUND_KEY_BLOCKING_DISABLED, this.eaep$blockingDisabled);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void eap$readAdvancedFromNbt(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains(COMPOUND_KEY_BLOCKING))
            this.eaep$smartBlocking = tag.getBoolean(COMPOUND_KEY_BLOCKING);
        if (tag.contains(COMPOUND_KEY_BLOCKING_DISABLED))
            this.eaep$blockingDisabled = tag.getBoolean(COMPOUND_KEY_BLOCKING_DISABLED);
    }

    // 在 pushPattern 中，重定向对 adapter.containsPatternInput(...) 的调用
    @Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Lappeng/helpers/patternprovider/PatternProviderTarget;containsPatternInput(Ljava/util/Set;)Z"))
    private boolean eap$redirectBlockingContains(PatternProviderTarget adapter,
                                                 java.util.Set<AEKey> patternInputs,
                                                 IPatternDetails patternDetails,
                                                 appeng.api.stacks.KeyCounter[] inputHolder) {
        // 原版是否打开阻挡
        boolean vanillaBlocking = ((AdvPatternProviderLogic)(Object)this).isBlocking();
        if (!vanillaBlocking) {
            return adapter.containsPatternInput(patternInputs);
        }

        // 仅当高级阻挡启用时启用“匹配则不阻挡”
        if (this.eaep$smartBlocking) {
            if (eap$targetFullyMatchesPatternInputs(adapter, patternDetails)) {
                // 返回 false 表示“不包含阻挡关键物”，从而不触发 continue，允许发配
                return false;
            }
        }
        // 否则使用原判定
        return adapter.containsPatternInput(patternInputs);
    }

    @Unique
    private boolean eap$targetFullyMatchesPatternInputs(PatternProviderTarget adapter, IPatternDetails patternDetails) {
        for (IInput in : patternDetails.getInputs()) {
            boolean slotMatched = false;
            for (GenericStack candidate : in.getPossibleInputs()) {
                AEKey key = candidate.what().dropSecondary();
                if (adapter.containsPatternInput(Collections.singleton(key))) {
                    slotMatched = true;
                    break;
                }
            }
            if (!slotMatched) {
                return false; // 任一输入槽未匹配则失败
            }
        }
        return true; // 每个输入槽都至少匹配了一个候选输入
    }
}
