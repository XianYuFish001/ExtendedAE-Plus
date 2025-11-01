package com.extendedae_plus.client;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.core.AEConfig;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.integration.RecipeViewer.RecipeViewerHelper;
import com.extendedae_plus.mixin.ae2.accessor.MEStorageScreenAccessor;
import com.extendedae_plus.mixin.extendedae.accessor.GuiExPatternTerminalAccessor;
import com.extendedae_plus.network.CPacketPullFromNetwork;
import com.extendedae_plus.network.CPacketTargetKeyTriggered;
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
public final class InputEvents {
    private static boolean isPulled;

    @SubscribeEvent
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().screen == null) return;

        if (event.getAction() != GLFW.GLFW_PRESS && isPulled) {
            isPulled = false;
            event.setCanceled(true);
            return;
        }

        var pulled = RecipeViewerHelper.getPulled(event.getButton());
        if (!RecipeViewerHelper.isCheatMode() && pulled.getFirst() > 0) {
            List<GenericStack> stacks = RecipeViewerHelper.getHoveredStacks();
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
            List<GenericStack> stacks = RecipeViewerHelper.getHoveredStacks();
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
            GenericStack stack = RecipeViewerHelper.getHoveredStacks().getFirst();
            if (stack == null) return;
            String name = stack.what().getDisplayName().getString();

            // 写入 AE2 终端的搜索框
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof MEStorageScreen<?> me) {
                try {
                    // 如果用EMI搜索框
                    if (AEConfig.instance().isUseExternalSearch()) RecipeViewerHelper.setSearchText(name);
                    else {
                        MEStorageScreenAccessor acc = (MEStorageScreenAccessor) me;
                        acc.eap$getSearchField().setValue(name);
                        acc.eap$setSearchText(name); // 同步到 Repo 并刷新
                    }
                    event.setCanceled(true);
                } catch (Throwable ignored) {
                }
            } else if (screen instanceof GuiExPatternTerminal<?> gpt) {
                try {
                    if (AEConfig.instance().isUseExternalSearch()) RecipeViewerHelper.setSearchText(name);
                    else {
                        GuiExPatternTerminalAccessor acc = (GuiExPatternTerminalAccessor) gpt;
                        acc.getSearchField().setValue(name);
                    }
                    event.setCanceled(true);
                } catch (Throwable ignored) {
                }
            }
        } else if (event.getKeyCode() == GLFW.GLFW_KEY_LEFT_CONTROL)
            PacketDistributor.sendToServer(new CPacketTargetKeyTriggered(CPacketTargetKeyTriggered.KeyType.CTRL_DOWN));
    }

    @SubscribeEvent
    public static void onKeyReleasePre(ScreenEvent.KeyReleased.Pre event) {
        if (Minecraft.getInstance().player == null) return;
        if (event.getKeyCode() == GLFW.GLFW_KEY_LEFT_CONTROL)
            PacketDistributor.sendToServer(new CPacketTargetKeyTriggered(CPacketTargetKeyTriggered.KeyType.CTRL_UP));
    }
}
