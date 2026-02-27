package com.extendedae_plus.mixin.core.ae2;

import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.init.EAEPDataComponents;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.fish.fishlib.util.keyBuilder.Patterns;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EncodedPatternItem.class)
public class MixinPatternEncodedProfile {
    // 客户端：在 HoverText 显示样板的编码玩家
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    public void appendEncoderProfileTooltip(ItemStack stack,
                                            Item.TooltipContext context,
                                            List<Component> lines,
                                            TooltipFlag advancedTooltips,
                                            CallbackInfo ci) {
        if (!EAEPConfig.INSTANCE.getShowPatternEncoder()) return;
        if (!stack.has(EAEPDataComponents.ProfileEncoder)) return;

        var data = stack.get(EAEPDataComponents.ProfileEncoder);
        if (data == null) return;
        lines.add(UtilKeyBuilder.INSTANCE.of(Patterns.Tooltip)
                .item(AEItems.PROCESSING_PATTERN.get())
                .addStr("encoder")
                .args(data.name())
                .build()
                .withStyle(ChatFormatting.GRAY));
    }
}
