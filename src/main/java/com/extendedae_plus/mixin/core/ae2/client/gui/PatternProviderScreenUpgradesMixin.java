package com.extendedae_plus.mixin.core.ae2.client.gui;

import appeng.api.upgrades.Upgrades;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.mixin.helper.IStyleAccessor;
import com.extendedae_plus.mixin.helper.IUpgradableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = PatternProviderScreen.class, priority = 1500, remap = false)
public abstract class PatternProviderScreenUpgradesMixin<C extends PatternProviderMenu> extends AEBaseScreen<C> {
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void eap$initUpgrades(PatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        if (ModList.get().isLoaded("appflux")) return;

        // 检查是否已经存在upgrades widget
        try {
            this.widgets.add("upgrades", new UpgradesPanel(menu.getSlots(SlotSemantics.UPGRADE), this::eap$getCompatibleUpgrades));
        } catch (IllegalStateException e) {
            ExtendedAEPlus.LOGGER.warn("Upgrades panel already exists");
            return;
        }

        // 设置TOOLBOX样式（完全按照AppliedFlux的方式）
        var sp = new SlotPosition();
        sp.setBottom(84);
        sp.setRight(1);
        sp.setGrid(SlotGridLayout.BREAK_AFTER_3COLS);
        var ws = new WidgetStyle();
        ws.setRight(2);
        ws.setBottom(90);
        ws.setWidth(59);
        ws.setHeight(66);
        style.getSlots().put("TOOLBOX", sp);
        ((IStyleAccessor) style).getImages().put("toolbox", Blitter.texture("guis/extra_panels.png", 128, 128).src(69, 62, 59, 66));
        ((IStyleAccessor) style).getWidgets().put("toolbox", ws);

        // 添加工具箱面板
        if (menu instanceof IUpgradableMenu upg && upg.eap$getToolbox() != null && upg.eap$getToolbox().isPresent()) {
            try {
                this.widgets.add("toolbox", new ToolboxPanel(style, upg.eap$getToolbox().getName()));
            } catch (IllegalStateException e) {
                ExtendedAEPlus.LOGGER.warn("Toolbox panel already exists");
            }
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
