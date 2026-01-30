package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AEItemKey.class)
public interface AccessorItemKey {
    // !?强强?!
    @Invoker("<init>")
    static AEItemKey eaep$newInstance(ItemStack stack) {
        throw new AssertionError();
    }
}
