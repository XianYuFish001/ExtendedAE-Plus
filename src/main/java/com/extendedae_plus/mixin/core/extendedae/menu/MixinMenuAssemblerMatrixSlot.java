package com.extendedae_plus.mixin.core.extendedae.menu;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.glodblock.github.extendedae.container.ContainerAssemblerMatrix$PatternSlotTracker")
public class MixinMenuAssemblerMatrixSlot {
    @Shadow
    @Mutable
    @Final
    private InternalInventory client;
    @Shadow
    @Final
    private InternalInventory server;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(TileAssemblerMatrixPattern host, CallbackInfo ci) {
        this.client = new AppEngInternalInventory(this.server.size());
    }
}
