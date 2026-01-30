package com.extendedae_plus.mixin.core.advancedae.logic;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.registry.dataComponent.DataEncoderProfile;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.pedroksl.advanced_ae.common.patterns.AdvPatternDetailsEncoder;
import net.pedroksl.advanced_ae.gui.AdvPatternEncoderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.List;

@Mixin(AdvPatternEncoderMenu.class)
public class MixinAdvEncoding extends AEBaseMenu {
    public MixinAdvEncoding(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Redirect(method = {"decodeProcessingPattern", "update"},
            at = @At(value = "INVOKE", target = "Lnet/pedroksl/advanced_ae/common/patterns/AdvPatternDetailsEncoder;encodeProcessingPattern(Ljava/util/List;Ljava/util/List;Ljava/util/HashMap;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack appendEncoder(List<GenericStack> sparseInputs,
                                    List<GenericStack> sparseOutputs,
                                    HashMap<AEKey, Direction> dirMap) {
        var stack = AdvPatternDetailsEncoder.encodeProcessingPattern(sparseInputs, sparseOutputs, dirMap);
        stack.set(ModDataComponents.DATA_ENCODER_PROFILE, new DataEncoderProfile(this.getPlayer().getGameProfile()));
        return stack;
    }
}
