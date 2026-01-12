package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.api.inventories.InternalInventory;
import appeng.menu.implementations.PatternAccessTermMenu;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatternAccessTermMenu.class)
public interface AccessorAccessMenu {
    @Accessor("byId")
    Long2ObjectOpenHashMap<AccessorContainerTracker> getIDMap();

    @Mixin(targets = "appeng.menu.implementations.PatternAccessTermMenu$ContainerTracker")
    interface AccessorContainerTracker {
        @Accessor("server")
        InternalInventory getInv();
    }
}
