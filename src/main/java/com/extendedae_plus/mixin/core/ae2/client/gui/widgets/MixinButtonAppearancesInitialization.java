package com.extendedae_plus.mixin.core.ae2.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.ButtonToolTips;
import com.extendedae_plus.common.init.ModSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SettingToggleButton.class)
public class MixinButtonAppearancesInitialization {
    @Shadow
    private Enum<?> currentValue;

    @Inject(method = "getIcon", at = @At("RETURN"), cancellable = true)
    private void findIconOnEAEPRegistries(CallbackInfoReturnable<Icon> cir) {
        if (!Icon.TOOLBAR_BUTTON_BACKGROUND.equals(cir.getReturnValue())) return;
        var appearance = ModSettings.appearances.get(this.currentValue);
        if (appearance == null) return;
        cir.setReturnValue(appearance.icon());
    }

    @Inject(method = "getItemOverlay", at = @At("RETURN"), cancellable = true)
    private void findItemOnEAEPRegistries(CallbackInfoReturnable<Item> cir) {
        if (cir.getReturnValue() != null) return;
        var appearance = ModSettings.appearances.get(this.currentValue);
        if (appearance == null) return;
        cir.setReturnValue(appearance.item());
    }

    @Inject(method = "getTooltipMessage", at = @At("RETURN"), cancellable = true)
    private void findTooltipOnEAEPRegistries(CallbackInfoReturnable<List<Component>> cir) {
        if (!cir.getReturnValue().isEmpty()
                && !ButtonToolTips.NoSuchMessage.text().toString()
                .equals(cir.getReturnValue().getFirst().toString()))
            return;
        var appearance = ModSettings.appearances.get(this.currentValue);
        if (appearance == null) return;
        cir.setReturnValue(appearance.tooltipLines());
    }
}
