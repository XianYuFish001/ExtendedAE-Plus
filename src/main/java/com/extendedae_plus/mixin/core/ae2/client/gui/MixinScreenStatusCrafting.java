package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftingCPUMenu;
import com.extendedae_plus.mixin.core.overrider.OverriderScreenCraftingCPU;
import com.extendedae_plus.network.CPacketOpenScreenCraftingNodeMachine;
import com.extendedae_plus.network.CPacketOpenScreenCraftingNodeProvider;
import com.extendedae_plus.util.UtilClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingCPUScreen.class)
public class MixinScreenStatusCrafting<TMenu extends CraftingCPUMenu> extends AEBaseScreen<TMenu> {
    public MixinScreenStatusCrafting(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Dynamic(mixin = OverriderScreenCraftingCPU.class)
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!UtilClient.ctrl()) return;
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;

        var hovered = this.getStackUnderMouse(mouseX, mouseY);
        if (hovered == null || hovered.stack() == null) return;
        var key = hovered.stack().what();

        var packet = switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> new CPacketOpenScreenCraftingNodeMachine(key);
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> new CPacketOpenScreenCraftingNodeProvider(key);
            default -> null;
        };
        PacketDistributor.sendToServer(packet);
        cir.setReturnValue(true);
    }
}
