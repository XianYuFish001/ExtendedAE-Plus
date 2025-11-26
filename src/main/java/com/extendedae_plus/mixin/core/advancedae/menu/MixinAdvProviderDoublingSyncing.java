package com.extendedae_plus.mixin.core.advancedae.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import com.extendedae_plus.mixin.impl.bridge.ISmartDoublingObject;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartDoubling;
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
public abstract class MixinAdvProviderDoublingSyncing implements SyncerSmartDoubling {
    @Final
    @Shadow(remap = false)
    protected AdvPatternProviderLogic logic;

    @Unique
    @GuiSync(25)
    public boolean eap$SmartDoubling = false;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void eap$syncSmartDoubling(CallbackInfo ci) {
        if (!((AEBaseMenu) (Object) this).isClientSide()) {
            var l = this.logic;
            if (l instanceof ISmartDoublingObject holder) {
                this.eap$SmartDoubling = holder.eaep$getDoublingState();
            }
        }
    }

    @Override
    public boolean eaep$getDoublingState() {
        return this.eap$SmartDoubling;
    }
}
