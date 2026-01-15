package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.client.render.widgets.button.EAEPCycleButton;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.registry.settings.ModeEncodingTransfer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternEncodingTermScreen.class)
public class MixinEncodingScreenTransfer<TMenu extends PatternEncodingTermMenu> extends MEStorageScreen<TMenu> {
    public MixinEncodingScreenTransfer(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(PatternEncodingTermMenu menu,
                        Inventory playerInventory,
                        Component title,
                        ScreenStyle style,
                        CallbackInfo ci) {
        var buttonTransferMode = new EAEPCycleButton.Builder()
                .globalTask(this::eaep$switchTransferMode)
                .addPart(EAEPActionItems.MERGE_NONE)
                .addPart(EAEPActionItems.MERGE_ADJACENCY)
                .addPart(EAEPActionItems.MERGE_INDEPENDENCE)
                .build();
        this.addToLeftToolbar(buttonTransferMode);
    }
    
    @Unique
    private void eaep$switchTransferMode(EAEPActionItems action) {
        this.menu.getConfigManager().putSetting(ModSettings.TRANSFER_MODE, switch (action) {
            case MERGE_NONE -> ModeEncodingTransfer.NONE;
            case MERGE_ADJACENCY -> ModeEncodingTransfer.MERGE_ADJACENCY;
            case MERGE_INDEPENDENCE -> ModeEncodingTransfer.INDEPENDENCE;
            case null, default -> null;
        });
    }
}
