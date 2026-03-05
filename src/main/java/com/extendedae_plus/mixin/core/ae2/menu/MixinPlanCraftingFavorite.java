package com.extendedae_plus.mixin.core.ae2.menu;

import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.extendedae_plus.integration.impl.recipeViewer.HelperRecipeViewer;
import com.extendedae_plus.util.UtilClient;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftConfirmMenu.class)
public class MixinPlanCraftingFavorite extends AEBaseMenu {
    @Shadow
    private CraftingPlanSummary plan;

    public MixinPlanCraftingFavorite(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(method = "goBack", at = @At("RETURN"))
    private void goBack(CallbackInfo ci) {
        if (this.isServerSide()) return;
        if (!UtilClient.ctrl()) return;

        if (this.plan == null) return;
        var entries = this.plan.getEntries();
        if (entries.isEmpty()) return;

        entries.forEach(entry -> {
            if (entry.getMissingAmount() <= 0) return;
            HelperRecipeViewer.addFavorite(new GenericStack(entry.getWhat(), entry.getMissingAmount()));
        });
    }
}
