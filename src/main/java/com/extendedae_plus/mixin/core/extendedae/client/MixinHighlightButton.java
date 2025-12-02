package com.extendedae_plus.mixin.core.extendedae.client;

import appeng.core.definitions.AEBlocks;
import com.extendedae_plus.mixin.core.extendedae.accessor.AccessorHighlightButton;
import com.extendedae_plus.mixin.core.extendedae.accessor.GuiExPatternTerminalAccessor;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.glodblock.github.extendedae.client.button.HighlightButton;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal;
import com.glodblock.github.extendedae.common.EAESingletons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;

@Mixin(value = HighlightButton.class)
public abstract class MixinHighlightButton {
    @Inject(method = "highlight", at = @At("TAIL"), remap = false)
    private static void onHighlight(Button uncheckedButton, CallbackInfo ci) {
        if (!(uncheckedButton instanceof AccessorHighlightButton accessorButton)) return;
        if (!(Minecraft.getInstance().screen instanceof GuiExPatternTerminal<?> screenTerminal)) return;
        if (!(screenTerminal instanceof GuiExPatternTerminalAccessor accessorTerminal)) return;

        var selectedProviderID = new Long[]{null};
        var craftingProvider = new boolean[]{false};
        accessorTerminal.getInfoMap().forEach((providerID, providerInfo) -> {
            if (providerInfo.pos() == null) return;
            if (!accessorButton.getPos().equals(providerInfo.pos())) return;
            if (!((accessorButton.getFace() == null && providerInfo.face() == null)
                    || accessorButton.getFace() == providerInfo.face())) return;

            // 适配EAE的新Feature, 采用同样的检测流程并取消对分子装配室供应器的上传
            var providerRecord = accessorTerminal.getIDMap().get(providerID);
            if (providerRecord == null) return;
            var icon = providerRecord.getGroup().icon();
            craftingProvider[0] = icon != null
                    && (icon.is(AEBlocks.MOLECULAR_ASSEMBLER.asItem())
                    || icon.is(EAESingletons.ASSEMBLER_MATRIX_PATTERN)
                    || icon.is(EAESingletons.EX_ASSEMBLER));

            selectedProviderID[0] = providerID;
        });

        if (selectedProviderID[0] == null) return;
        if (craftingProvider[0]) return;
        try {
            var setter = screenTerminal.getClass().getMethod("eaep$chooseProvider", long.class);
            setter.setAccessible(true);
            setter.invoke(screenTerminal, selectedProviderID[0]);

            var player = Minecraft.getInstance().player;
            if (player == null) return;
            player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.message)
                    .addStr("provider_to_upload")
                    .addStr("selected")
                    .args(selectedProviderID[0])
                    .build(), false);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
        }

        // 我真服了, 这才是反射大王, 整整6个啊😅
    }
}