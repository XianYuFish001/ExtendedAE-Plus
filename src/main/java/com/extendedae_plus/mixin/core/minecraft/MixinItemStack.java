package com.extendedae_plus.mixin.core.minecraft;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.util.UtilTextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void onTooltip(Item.TooltipContext tooltipContext,
                           Player player,
                           TooltipFlag tooltipFlag,
                           CallbackInfoReturnable<List<Component>> cir) {
        var texts = cir.getReturnValue();
        if (texts.isEmpty()) return;

        if (!ExtendedAEPlus.MODNAME.equals(texts.getLast().getString())) return;
        texts.removeLast();
        texts.add(UtilTextComponent.modNameColorful.copy().withStyle(ChatFormatting.ITALIC));
    }
}
