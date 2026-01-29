package com.extendedae_plus.client.render.widgets.button;

import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.core.network.serverbound.ConfigButtonPacket;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartBlocking;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartDoubling;
import com.extendedae_plus.mixin.impl.widget.HelperRenderablesModifier;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class ButtonImplementations {
    public static EAEPServerCycleButton buttonBlocking(AbstractContainerMenu menu) {
        EAEPServerCycleButton button = new EAEPServerCycleButton.Builder()
                .setTask(new ConfigButtonPacket(ModSettings.smartBlocking, false))
                .addPart(EAEPActionItems.blockingDisabled)
                .addPart(EAEPActionItems.blockingEnabled)
                .addPart(EAEPActionItems.blockingUnable)
                .setIterator((prev, reversed) -> (prev + (reversed ? -1 : 1)) % 2)
                .setSyncer(() -> {
                    if (!(menu instanceof SyncerSmartBlocking syncer)) return 0;
                    else return switch (syncer.eaep$getBlockingState()) {
                        case DISABLED -> 0;
                        case ENABLED -> 1;
                        case DISABLED_BY_SUPER -> 2;
                    };
                })
                .build();
        button.updateState();
        return button;
    }

    public static EAEPServerCycleButton buttonDoubling(AbstractContainerMenu menu) {
        EAEPServerCycleButton button = new EAEPServerCycleButton.Builder()
                .setTask(new ConfigButtonPacket(ModSettings.smartDoubling, false))
                .addPart(EAEPActionItems.doublingDisabled)
                .addPart(EAEPActionItems.doublingEnabled)
                .setSyncer(() -> menu instanceof SyncerSmartDoubling syncer
                        && YesNo.YES.equals(syncer.eaep$getDoublingState()))
                .build();
        button.updateState();
        return button;
    }

    public static <T extends AEBaseScreen<?>> Pair<Integer, Integer>
    updateScalingButtonsLayout(T screen, int bx, int by, @Nullable Pair<Integer, Integer> lastScreenInfo) {
        if (!(screen instanceof HelperProviderButtons helper)) return lastScreenInfo;

        boolean flagReplaceButton = lastScreenInfo == null
                || screen.width != lastScreenInfo.getFirst()
                || screen.height != lastScreenInfo.getSecond();
        if (flagReplaceButton) lastScreenInfo = new Pair<>(screen.width, screen.height);

        int spacing = helper.eaep$getButtons().getFirst().getHeight() + 6;
        helper.eaep$getButtons().forEach(button -> {
            if (button == null) return;
            button.setVisibility(true);
            if (!screen.renderables.contains(button))
                HelperRenderablesModifier.addRenderableWidget(screen, button);

            if (flagReplaceButton) {
                HelperRenderablesModifier.removeWidget(screen, button);
                HelperRenderablesModifier.addRenderableWidget(screen, button);
            }

            button.setX(bx);
            button.setY(by + spacing * helper.eaep$getButtons().indexOf(button));
        });

        return lastScreenInfo;
    }
}
