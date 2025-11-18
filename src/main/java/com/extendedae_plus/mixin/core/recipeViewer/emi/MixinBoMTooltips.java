package com.extendedae_plus.mixin.core.recipeViewer.emi;

import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.util.UtilGetKey;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.screen.tooltip.EmiTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@MixinDependencies("emi")
@Mixin(BoMScreen.class)
public class MixinBoMTooltips {
    @ModifyArg(method = "render", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/EmiRenderHelper;drawTooltip(Lnet/minecraft/client/gui/screens/Screen;Ldev/emi/emi/runtime/EmiDrawContext;Ljava/util/List;III)V"),
            index = 2, remap = false)
    private List<ClientTooltipComponent> modifyBoMHelpTooltip(List<ClientTooltipComponent> components) {
        var componentsMutable = new ArrayList<>(components);
        componentsMutable.addAll(EmiTooltip.splitTranslate(new UtilGetKey(UtilGetKey.tooltip)
                .addStr("bom")
                .addStr("help")
                .buildRaw()));
        return componentsMutable;
    }
}
