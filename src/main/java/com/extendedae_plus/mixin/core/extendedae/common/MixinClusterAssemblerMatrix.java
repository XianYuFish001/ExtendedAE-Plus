package com.extendedae_plus.mixin.core.extendedae.common;

import com.extendedae_plus.common.block.assemblerMatrix.coreAdvancedCrafter.BlockEntityAdvancedCrafter;
import com.extendedae_plus.mixin.impl.bridge.HelperAssemblerMatrixModifier;
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClusterAssemblerMatrix.class)
public class MixinClusterAssemblerMatrix implements HelperAssemblerMatrixModifier {
    @Shadow
    @Final
    private ReferenceSet<TileAssemblerMatrixCrafter> availableCrafters;
    @Shadow
    @Final
    private ReferenceSet<TileAssemblerMatrixCrafter> busyCrafters;
    @Shadow
    @Final
    private Reference2IntMap<TileAssemblerMatrixCrafter> crafterStatusCache;
    @Shadow
    private int speedCore;

    @Unique
    private boolean eaep$uploadCore = false;

    @Override
    public void eaep$addCrafter(TileAssemblerMatrixCrafter crafter) {
        if (crafter.usedThread() < BlockEntityAdvancedCrafter.getMaxThread())
            this.availableCrafters.add(crafter);
        else this.busyCrafters.add(crafter);
    }

    @Override
    public void eaep$addSpeedCore() {
        this.speedCore++;
    }

    @Override
    public void eaep$updateCrafter(TileAssemblerMatrixCrafter crafter) {
        if (this.crafterStatusCache.containsKey(crafter)) {
            int previous = this.crafterStatusCache.getInt(crafter);
            if (previous == crafter.usedThread()) {
                return;
            }
        }

        this.crafterStatusCache.put(crafter, crafter.usedThread());
        this.availableCrafters.remove(crafter);
        this.busyCrafters.remove(crafter);
        this.eaep$addCrafter(crafter);
    }

    @Override
    public void eaep$markUploadCore() {
        this.eaep$uploadCore = true;
    }

    @Override
    public boolean eaep$hasUploadCore() {
        return this.eaep$uploadCore;
    }
}
