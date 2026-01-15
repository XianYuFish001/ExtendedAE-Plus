package com.extendedae_plus.mixin.core.ae2.logic;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.common.init.ModSettings;
import com.google.common.math.LongMath;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(EncodingHelper.class)
public class MixinEncodingTransfer {
    @Inject(method = "addOrMerge", at = @At("HEAD"), cancellable = true)
    private static void onTransferAdding(List<GenericStack> stacks, GenericStack newStack, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof PatternEncodingTermMenu menu)) return;
        switch (menu.getConfigManager().getSetting(ModSettings.TRANSFER_MODE)) {
            case INDEPENDENCE -> {
                stacks.add(newStack);
                ci.cancel();
            }
            case MERGE_ADJACENCY -> {
                if (stacks.isEmpty()) return;
                var existingStack = stacks.getLast();
                if (Objects.equals(existingStack.what(), newStack.what())) {
                    var newAmount = LongMath.saturatedAdd(existingStack.amount(), newStack.amount());
                    stacks.removeLast();
                    stacks.addLast(new GenericStack(newStack.what(), newAmount));

                    var overflow = newStack.amount() - (newAmount - existingStack.amount());
                    if (overflow > 0) stacks.add(new GenericStack(newStack.what(), overflow));
                } else stacks.add(newStack);

                ci.cancel();
            }
        }
    }
}
