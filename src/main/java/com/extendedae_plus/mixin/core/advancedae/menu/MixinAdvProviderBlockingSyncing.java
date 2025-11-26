package com.extendedae_plus.mixin.core.advancedae.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import com.extendedae_plus.mixin.impl.bridge.ISmartBlockingObject;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartBlocking;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvPatternProviderMenu.class)
public abstract class MixinAdvProviderBlockingSyncing implements SyncerSmartBlocking {
    @Final
    @Shadow(remap = false)
    protected AdvPatternProviderLogic logic;

    // 选择一个未占用的 GUI 同步 id（AE2 已用到 7），这里使用 20 以避冲突
    @Unique
    @GuiSync(22)
    public boolean eaep$smartBlocking = false;
    @Unique
    @GuiSync(23)
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
