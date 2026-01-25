package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.style.WidgetStyle;
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorStyleWidgets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.regex.Pattern;

@Mixin(ScreenStyle.class)
public class MixinStyleExternal {
    @Shadow
    @Final
    private Map<String, WidgetStyle> widgets;

    @Unique
    private AccessorStyleWidgets eaep$styleExternal = null;
    @Unique
    private static final Pattern eaep$patternIDExternal = Pattern.compile("external\\.\\S+\\.\\S+");

    @Inject(method = "getWidget", at = @At("HEAD"), cancellable = true)
    private void getWidget(String id, CallbackInfoReturnable<WidgetStyle> cir) {
        if (!eaep$patternIDExternal.matcher(id).matches()) return;

        var existing = this.widgets.get(id);
        if (existing != null) cir.setReturnValue(existing);

        if (this.eaep$styleExternal == null)
            this.eaep$styleExternal = (AccessorStyleWidgets) StyleManager.loadStyleDoc("/screens/extendedae_plus/external.json");
        var external = this.eaep$styleExternal.getWidgets().get(id.substring(9));
        if (external == null) return;
        this.widgets.put(id, external);
    }
}
