package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.api.upgrades.Upgrades;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.mixin.MixinDependencies;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@MixinDependencies(conflict = "appflux")
@Mixin(value = PatternProviderScreen.class, priority = 1100, remap = false)
public abstract class PatternProviderScreenUpgradesMixin<C extends PatternProviderMenu> extends AEBaseScreen<C> {
    @Unique
    private static final Logger eaep$LOGGER = LogUtils.getLogger();

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void eap$initUpgrades(PatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        // 检查是否已经存在upgrades widget
        try {
            this.widgets.add("upgrades", new UpgradesPanel(menu.getSlots(SlotSemantics.UPGRADE), this::eap$getCompatibleUpgrades));
        } catch (IllegalStateException e) {
            eaep$LOGGER.warn("Upgrades panel already exists");
        }
    }

    @Unique
    private List<Component> eap$getCompatibleUpgrades() {
        var list = new ArrayList<Component>();
        list.add(GuiText.CompatibleUpgrades.text());
        var target = menu.getTarget();
        if (target instanceof PatternProviderLogicHost host) {
            list.addAll(Upgrades.getTooltipLinesForMachine(host.getTerminalIcon().getItem()));
        }
        return list;
    }

    public PatternProviderScreenUpgradesMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}
