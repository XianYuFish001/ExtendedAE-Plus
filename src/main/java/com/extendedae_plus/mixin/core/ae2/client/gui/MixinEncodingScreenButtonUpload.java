package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.network.CPacketRequestUploading;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternEncodingTermScreen.class)
public abstract class MixinEncodingScreenButtonUpload<TMenu extends PatternEncodingTermMenu> extends MEStorageScreen<TMenu> {
    public MixinEncodingScreenButtonUpload(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(PatternEncodingTermMenu menu,
                        Inventory playerInventory,
                        Component title,
                        ScreenStyle style,
                        CallbackInfo ci) {
        if (!EAEPConfig.independentUploadButton.get()) return;
        var buttonUpload = new EAEPActionButton(EAEPActionItems.patternUpload,
                $ -> PacketDistributor.sendToServer(CPacketRequestUploading.INSTANCE));
        buttonUpload.setScale(0.75F);
        this.widgets.add("external.screen_encode.button_upload", buttonUpload);
    }
}
