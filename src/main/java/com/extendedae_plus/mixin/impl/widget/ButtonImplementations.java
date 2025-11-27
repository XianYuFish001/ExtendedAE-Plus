package com.extendedae_plus.mixin.impl.widget;

import appeng.client.gui.AEBaseScreen;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartBlocking;
import com.extendedae_plus.mixin.impl.bridge.SyncerSmartDoubling;
import com.extendedae_plus.network.CPacketToggleSmartBlocking;
import com.extendedae_plus.network.CPacketToggleSmartDoubling;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class ButtonImplementations {
    public static EAEPServerCycleButton buttonBlocking(AbstractContainerMenu menu) {
        EAEPServerCycleButton button = new EAEPServerCycleButton.Builder()
                .setTask(CPacketToggleSmartBlocking.INSTANCE)
                .addPart(EAEPActionItems.BLOCKING_DISABLED)
                .addPart(EAEPActionItems.BLOCKING_ENABLED)
                .addPart(EAEPActionItems.BLOCKING_DISABLED_BY_SUPER)
                .setIterator(index -> (index + 1) % 2)
                .setSyncedStateGetter(() -> {
                    if (!(menu instanceof SyncerSmartBlocking syncer)) return 0;
                    else if (syncer.eaep$isBlockingDisabled()) return 2;
                    else if (syncer.eaep$getBlockingState()) return 1;
                    else return 0;
                })
                .build();
        button.updateState();
        return button;
    }

    public static EAEPServerCycleButton buttonDoubling(AbstractContainerMenu menu) {
        EAEPServerCycleButton button = new EAEPServerCycleButton.Builder()
                .setTask(CPacketToggleSmartDoubling.INSTANCE)
                .addPart(EAEPActionItems.DOUBLING_DISABLED)
                .addPart(EAEPActionItems.DOUBLING_ENABLED)
                .setSyncedStateGetter(() -> menu instanceof SyncerSmartDoubling syncer && syncer.eaep$getDoublingState())
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
