package com.extendedae_plus.mixin.ae2.menu;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.glodblock.github.glodium.network.packet.sync.IActionHolder;
import com.glodblock.github.glodium.network.packet.sync.Paras;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 给 AE2 的 PatternEncodingTermMenu 增加一个通用动作持有者，实现接收 EPP 的 CGenericPacket 动作。
 */
@Mixin(PatternEncodingTermMenu.class)
public abstract class ContainerPatternEncodingTermMenuMixin implements IActionHolder {

    @Unique
    private final Map<String, Consumer<Paras>> eap$actions = createHolder();

    @Unique
    private Player epp$player;

    // 有没有可能另一个构造也会调用这个构造呢? 写两遍是有什么心事吗?
    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V", at = @At("TAIL"), remap = false)
    private void eap$ctor(net.minecraft.world.inventory.MenuType<?> menuType, int id, net.minecraft.world.entity.player.Inventory ip, appeng.helpers.IPatternTerminalMenuHost host, boolean bindInventory, CallbackInfo ci) {
        this.epp$player = ip.player;
    }

    @NotNull
    @Override
    public Map<String, Consumer<Paras>> getActionMap() {
        return this.eap$actions;
    }

    // 服务器端：在 encode() 执行完毕后，如果已编码槽位存在样板且当前为“合成模式”，则上传到装配矩阵
    // 什么击败设计, 为什么合成样板就要直接上传?

    @Inject(method = "encodePattern", at = @At("RETURN"), remap = false, cancellable = true)
    private void onEncodePatternReturn(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = cir.getReturnValue();
        if (itemStack != null && !itemStack.isEmpty()) {
            itemStack.getOrCreateTag().putString("encodePlayer", this.epp$player.getGameProfile().getName());
            cir.setReturnValue(itemStack);
        }
    }
}
