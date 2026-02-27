package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.widgets.AETextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MEStorageScreen.class)
public interface AccessorScreenStorage {
    @Accessor("searchField")
    AETextField getFieldSearch();

    @Invoker("setSearchText")
    void eaep$setSearchText(String text);
}
