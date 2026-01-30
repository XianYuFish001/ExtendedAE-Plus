package com.extendedae_plus.mixin.core.overrider;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftingCPUMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingCPUScreen.class)
public class OverriderScreenCraftingCPU<TMenu extends CraftingCPUMenu> extends AEBaseScreen<TMenu> {
    public OverriderScreenCraftingCPU(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Unique
    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        return super.mouseClicked(xCoord, yCoord, btn);
    }
}
