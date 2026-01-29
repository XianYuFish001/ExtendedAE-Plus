package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.InterfaceMenu;
import com.extendedae_plus.client.render.widgets.button.ButtonImplementations;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.mixin.core.minecraft.accessor.AbstractContainerScreenAccessor;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderButtons;
import com.extendedae_plus.network.CPacketInterfaceScaling;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 在 AE2 的 ME 接口界面注入倍增/除法按钮（x2/÷2、x5/÷5、x10/÷10）。
 * 点击时通过 NeoForge 自定义负载发送到服务端调整配置数量。
 */
@Mixin(value = InterfaceScreen.class, remap = false)
public abstract class InterfaceScreenMixin<TMenu extends InterfaceMenu>
        extends AEBaseScreen<TMenu>
        implements HelperProviderButtons {
    @Unique
    public final List<EAEPActionButton> eaep$scalingButtons = new ArrayList<>();
    @Unique
    private Pair<Integer, Integer> eaep$lastScreenInfo;

    @Unique
    private int eap$lastConfigIndex = -1;

    public InterfaceScreenMixin(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addScaleButtons(CallbackInfo ci) {
        EAEPActionItems.actions.get("scaling").forEach(action ->
                this.eaep$scalingButtons.add(new EAEPActionButton(action, CPacketInterfaceScaling::send)));

        this.eaep$scalingButtons.forEach(button -> {
            this.addRenderableWidget(button);
            button.setVisibility(true);
        });
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void eap$ensureButtons(CallbackInfo ci) {
        try {
            this.eaep$updateButtonsStates();
            eap$updateLastConfigFromHover();
        } catch (Throwable ignored) {
        }
    }

    @Unique
    private void eap$updateLastConfigFromHover() {
        Slot hovered = ((AbstractContainerScreenAccessor<?>) this).eap$getHoveredSlot();
        if (hovered == null) {
            return;
        }
        var configSlots = this.getMenu().getSlots(SlotSemantics.CONFIG);
        if (configSlots == null || configSlots.isEmpty()) {
            return;
        }
        Integer idx = null;
        for (var s : configSlots) {
            if (s == hovered) {
                try {
                    var f = s.getClass().getDeclaredField("slot");
                    f.setAccessible(true);
                    Object v = f.get(s);
                    if (v instanceof Integer i) {
                        idx = i;
                    }
                } catch (Throwable ignored) {
                }
                if (idx == null) {
                    idx = configSlots.indexOf(s);
                }
                break;
            }
        }
        if (idx != null && idx >= 0) {
            if (eap$lastConfigIndex != idx) {
                eap$lastConfigIndex = idx;
            }
        }
    }

    @Override
    public void eaep$updateButtonsStates() {
        this.eaep$lastScreenInfo = ButtonImplementations.updateScalingButtonsLayout(
                this,
                this.leftPos + this.imageWidth + 3,
                this.topPos + 50,
                this.eaep$lastScreenInfo
        );
    }

    @Override
    public List<EAEPActionButton> eaep$getButtons() {
        return this.eaep$scalingButtons;
    }
}
