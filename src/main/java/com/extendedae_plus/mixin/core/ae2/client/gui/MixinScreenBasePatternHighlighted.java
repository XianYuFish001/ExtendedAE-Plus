package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.menu.AEBaseMenu;
import com.extendedae_plus.mixin.helper.HelperPatternHighlightable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(AEBaseScreen.class)
public abstract class MixinScreenBasePatternHighlighted<TMenu extends AEBaseMenu>
        extends AbstractContainerScreen<TMenu>
        implements HelperPatternHighlightable {
    @Unique
    private Consumer<GuiGraphics> eaep$patternHighlighter = $ -> {};

    public MixinScreenBasePatternHighlighted(TMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "drawFG", at = @At("RETURN"))
    private void renderHighlight(GuiGraphics guiGraphics,
                                 int offsetX,
                                 int offsetY,
                                 int mouseX,
                                 int mouseY,
                                 CallbackInfo ci) {
        this.eaep$patternHighlighter.accept(guiGraphics);
    }

    @Override
    public @NotNull List<@NotNull Slot> eaep$getSlots() {
        return this.menu.slots;
    }

    @Override
    public void eaep$drawHighlights(@NotNull Consumer<@NotNull GuiGraphics> drawer) {
        this.eaep$patternHighlighter = drawer;
    }
}
