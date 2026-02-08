package com.extendedae_plus.mixin.core.recipeViewer.emi.accessor;

import dev.emi.emi.bom.MaterialNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "dev.emi.emi.screen.BoMScreen$Hover")
public interface AccessorBoMScreenHover {
    @Accessor("node")
    MaterialNode getNode();
}
