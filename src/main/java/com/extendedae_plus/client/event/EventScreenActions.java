package com.extendedae_plus.client.event;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.core.AEConfig;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.integration.recipeViewer.HelperRecipeViewer;
import com.extendedae_plus.mixin.core.ae2.accessor.MEStorageScreenAccessor;
import com.extendedae_plus.mixin.core.extendedae.accessor.AccessorExAccessScreen;
import com.extendedae_plus.network.CPacketPullFromNetwork;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID, value = Dist.CLIENT)
public final class EventScreenActions {
    private static boolean isPulled;

    @SubscribeEvent
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().screen == null) return;

        if (HelperRecipeViewer.isCheatMode()) return;

        if (event.getAction() != GLFW.GLFW_PRESS) {
            if (isPulled) event.setCanceled(true);
            isPulled = false;
            return;
        }

        var pulled = HelperRecipeViewer.getPulled(event.getButton());
        if (pulled.getFirst() > 0) {
            List<GenericStack> stacks = HelperRecipeViewer.getHoveredStacks();
            GenericStack stack = stacks.isEmpty() ? null : stacks.getFirst();
            if (stack == null) return;

            PacketDistributor.sendToServer(new CPacketPullFromNetwork(
                    new GenericStack(stack.what(), pulled.getFirst()),
                    true,
                    pulled.getSecond()));
            isPulled = true;
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            List<GenericStack> stacks = HelperRecipeViewer.getHoveredStacks();
            GenericStack stack = stacks.isEmpty() ? null : stacks.getFirst();
            if (stack == null) return;

            PacketDistributor.sendToServer(new CPacketPullFromNetwork(stack, false, false));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKeyPressedPre(ScreenEvent.KeyPressed.Pre event) {
        if (Minecraft.getInstance().player == null) return;
        if (event.getKeyCode() == GLFW.GLFW_KEY_F) {
            // 仅当鼠标确实悬停在 JEI 配料上时触发
            // 大概会在一格有多个(?)stack的时候出bug, 但是真的会有那种时候吗?
            var stack = HelperRecipeViewer.getHoveredStacks().getFirst();
            if (stack == null) return;
            var name = stack.what().getDisplayName().getString();


            if (AEConfig.instance().isUseExternalSearch()) {
                HelperRecipeViewer.setSearchText(name);
                event.setCanceled(true);
                return;
            }

            var screen = Minecraft.getInstance().screen;
            if (screen instanceof MEStorageScreen<?> me) {
                MEStorageScreenAccessor acc = (MEStorageScreenAccessor) me;
                acc.eap$getSearchField().setValue(name);
                acc.eap$setSearchText(name);
                event.setCanceled(true);
            } else if (screen instanceof GuiExPatternTerminal<?> gpt) {
                AccessorExAccessScreen acc = (AccessorExAccessScreen) gpt;
                acc.getSearchField().setValue(name);
            }
            event.setCanceled(true);
        }
    }
}
