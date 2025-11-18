package com.extendedae_plus.mixin.core.ae2.menu;

import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.common.dataComponent.DataEncoderProfile;
import com.extendedae_plus.common.init.ModDataComponents;
import com.glodblock.github.glodium.network.packet.sync.ActionMap;
import com.glodblock.github.glodium.network.packet.sync.IActionHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 给 AE2 的 PatternEncodingTermMenu 增加一个通用动作持有者，实现接收 EPP 的 CGenericPacket 动作。
 * 注册动作 "upload_to_matrix"：仅上传“合成图样”到 ExtendedAE 装配矩阵。
 */
@Mixin(PatternEncodingTermMenu.class)
public abstract class ContainerPatternEncodingTermMenuMixin implements IActionHolder {
    @Unique
    private final ActionMap eap$actions = ActionMap.create();
    @Unique
    private Player eaep$player;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V", at = @At("CTOR_HEAD"))
    private void onInit(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host, boolean bindInventory, CallbackInfo ci) {
        this.eaep$player = ip.player;
    }

    @NotNull
    @Override
    public ActionMap getActionMap() {
        return this.eap$actions;
    }

    // 服务器端：在构造样板返回前插入编码玩家的名称
    @Inject(method = "encodePattern", at = @At("TAIL"), remap = false, cancellable = true)
    private void appendEncoderProfile(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = cir.getReturnValue();
        if (itemStack == null || itemStack.isEmpty()) return;

        try {
            itemStack.set(ModDataComponents.DATA_ENCODER_PROFILE,
                    new DataEncoderProfile(this.eaep$player.getGameProfile()));
        } catch (Throwable ignore) {
        }
        cir.setReturnValue(itemStack);
    }
}
