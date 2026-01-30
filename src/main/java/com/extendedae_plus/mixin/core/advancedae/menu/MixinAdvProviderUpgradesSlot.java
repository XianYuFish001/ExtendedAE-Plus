package com.extendedae_plus.mixin.core.advancedae.menu;

import appeng.menu.AEBaseMenu;
import com.extendedae_plus.mixin.MixinDependencies;
import com.extendedae_plus.mixin.bridge.HelperProviderUpgradesInv;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinDependencies(conflict = "appflux")
@Mixin(AdvPatternProviderMenu.class)
public class MixinAdvProviderUpgradesSlot extends AEBaseMenu {
    @Shadow
    @Final
    protected AdvPatternProviderLogic logic;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/pedroksl/advanced_ae/common/logic/AdvPatternProviderLogicHost;)V", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.setupUpgrades(((HelperProviderUpgradesInv)this.logic).eaep$getUpgradeInventory());
    }

    public MixinAdvProviderUpgradesSlot(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }
}
