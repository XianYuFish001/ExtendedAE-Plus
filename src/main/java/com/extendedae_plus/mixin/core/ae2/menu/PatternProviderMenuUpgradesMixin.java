package com.extendedae_plus.mixin.core.ae2.menu;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.impl.bridge.HelperProviderUpgradesInv;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinDependencies(conflict = "appflux")
@Mixin(value = PatternProviderMenu.class, priority = 1100, remap = false)
public abstract class PatternProviderMenuUpgradesMixin extends AEBaseMenu {
    @Final
    @Shadow
    protected PatternProviderLogic logic;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/patternprovider/PatternProviderLogicHost;)V",
            at = @At("TAIL"))
    private void eap$initUpgrades(MenuType<?> menuType, int id, Inventory playerInventory, PatternProviderLogicHost host, CallbackInfo ci) {
        this.setupUpgrades(((HelperProviderUpgradesInv) this.logic).eaep$getUpgradeInventory());
    }

    public PatternProviderMenuUpgradesMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }
}
