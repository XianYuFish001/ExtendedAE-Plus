package com.extendedae_plus.mixin.core.ae2.menu;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.mixin.impl.bridge.ISmartBlockingObject;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartBlocking;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternProviderMenu.class)
public abstract class MixinProviderBlockingSyncing implements SyncerSmartBlocking {
    @Final
    @Shadow
    protected PatternProviderLogic logic;

    // 选择一个未占用的 GUI 同步 id（AE2 已用到 7），这里使用 20 以避冲突
    @Unique
    @GuiSync(20)
    public boolean eaep$smartBlocking = false;
    @Unique
    @GuiSync(21)
    public boolean eaep$blockingDisabled = false;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void syncBlockingState(CallbackInfo ci) {
        // 避免@Shadow父类方法，改用公共API：AEBaseMenu#isClientSide()
        if (!((AEBaseMenu) (Object) this).isClientSide()) {
            var l = this.logic;
            if (l instanceof ISmartBlockingObject holder) {
                this.eaep$smartBlocking = holder.eaep$getBlockingState();
                this.eaep$blockingDisabled = holder.eaep$isBlockingDisabled();
            }
        }
    }

    @Override
    public boolean eaep$getBlockingState() {
        return this.eaep$smartBlocking;
    }

    @Override
    public boolean eaep$isBlockingDisabled() {
        return this.eaep$blockingDisabled;
    }
}