package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.client.render.widgets.button.EAEPCycleButton;
import com.extendedae_plus.common.init.EAEPSettings;
import com.extendedae_plus.common.registry.settings.ModeEncodingTransfer;
import com.fish.fishlib.util.UtilJava;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternEncodingTermScreen.class)
public class MixinScreenEncodingTransfer<TMenu extends PatternEncodingTermMenu> extends MEStorageScreen<TMenu> {
    public MixinScreenEncodingTransfer(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(PatternEncodingTermMenu menu,
                        Inventory playerInventory,
                        Component title,
                        ScreenStyle style,
                        CallbackInfo ci) {
        var buttonTransferMode = new EAEPCycleButton.Builder()
                .globalTask(UtilJava.consumerKotlin(this::eaep$switchTransferMode))
                .addPart(EAEPActionItems.MergeNone)
                .addPart(EAEPActionItems.MergeAdjacency)
                .addPart(EAEPActionItems.MergeIndependence)
                .build();
        this.addToLeftToolbar(buttonTransferMode);
    }
    
    @Unique
    private void eaep$switchTransferMode(EAEPActionItems action) {
        this.menu.getConfigManager().putSetting(EAEPSettings.modeTransfer, switch (action) {
            case MergeNone -> ModeEncodingTransfer.NONE;
            case MergeAdjacency -> ModeEncodingTransfer.MERGE_ADJACENCY;
            case MergeIndependence -> ModeEncodingTransfer.INDEPENDENCE;
            case null, default -> null;
        });
    }
}
