package com.extendedae_plus.mixin.core.advancedae.client.gui;

import appeng.api.upgrades.Upgrades;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import com.fish.fishlib.mixin.MixinDependencies;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.pedroksl.advanced_ae.client.gui.AdvPatternProviderScreen;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@MixinDependencies(conflict = "appflux")
@Mixin(AdvPatternProviderScreen.class)
public class MixinAdvProviderUpgradesWidget<TMenu extends AdvPatternProviderMenu> extends AEBaseScreen<TMenu> {
    @Unique
    private static final Logger eaep$LOGGER = LogUtils.getLogger();

    public MixinAdvProviderUpgradesWidget(TMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(AdvPatternProviderMenu menu,
                        Inventory playerInventory,
                        Component title,
                        ScreenStyle style,
                        CallbackInfo ci) {
        try {
            this.widgets.add("upgrades", new UpgradesPanel(
                    menu.getSlots(SlotSemantics.UPGRADE), this::eaep$getCompatibleUpgrades));
        } catch (IllegalStateException e) {
            eaep$LOGGER.warn("[EAEP/screen] Upgrades panel already exists");
        }
    }

    @Unique
    private List<Component> eaep$getCompatibleUpgrades() {
        var list = new ArrayList<Component>();
        list.add(GuiText.CompatibleUpgrades.text());
        var target = menu.getTarget();
        if (target instanceof AdvPatternProviderLogicHost host)
            list.addAll(Upgrades.getTooltipLinesForMachine(host.getTerminalIcon().getItem()));
        return list;
    }
}
