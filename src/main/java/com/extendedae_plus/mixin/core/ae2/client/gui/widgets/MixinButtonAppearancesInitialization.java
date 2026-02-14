package com.extendedae_plus.mixin.core.ae2.client.gui.widgets;

import appeng.api.config.Setting;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.ButtonToolTips;
import com.extendedae_plus.common.init.ModSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SettingToggleButton.class)
public class MixinButtonAppearancesInitialization<TEnum extends Enum<TEnum>> {
    @Shadow
    private TEnum currentValue;
    @Shadow
    @Final
    private Setting<TEnum> buttonSetting;

    @Inject(method = "getIcon", at = @At("RETURN"), cancellable = true)
    private void findIconOnEAEPRegistries(CallbackInfoReturnable<Icon> cir) {
        if (!Icon.TOOLBAR_BUTTON_BACKGROUND.equals(cir.getReturnValue())) return;
        var appearance = ModSettings.findAppearance(this.buttonSetting, this.currentValue);
        if (appearance == null) return;
        cir.setReturnValue(appearance.action.getAEIcon());
    }

    @Inject(method = "getItemOverlay", at = @At("RETURN"), cancellable = true)
    private void findItemOnEAEPRegistries(CallbackInfoReturnable<Item> cir) {
        if (cir.getReturnValue() != null) return;
        var appearance = ModSettings.findAppearance(this.buttonSetting, this.currentValue);
        if (appearance == null) return;
        cir.setReturnValue(appearance.item);
    }

    @Inject(method = "getTooltipMessage", at = @At("RETURN"), cancellable = true)
    private void findTooltipOnEAEPRegistries(CallbackInfoReturnable<List<Component>> cir) {
        if (!cir.getReturnValue().isEmpty()
                && !ButtonToolTips.NoSuchMessage.text().toString()
                .equals(cir.getReturnValue().getFirst().toString()))
            return;
        var appearance = ModSettings.findAppearance(this.buttonSetting, this.currentValue);
        if (appearance == null) return;
        if (appearance.action.getName().getString().isEmpty()) return;
        cir.setReturnValue(List.of(appearance.action.getName(), appearance.action.getTooltip()));
    }
}
