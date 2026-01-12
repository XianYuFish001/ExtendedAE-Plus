package com.extendedae_plus.mixin.core.ae2.menu;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.ITerminalHost;
import appeng.core.definitions.AEItems;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.impl.pattern.PatternUploader;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.registry.dataComponent.DataEncoderProfile;
import com.extendedae_plus.mixin.impl.bridge.BridgeCtrlPressed;
import com.extendedae_plus.mixin.impl.bridge.BridgePlanToEncode;
import com.extendedae_plus.mixin.impl.bridge.BridgeProviderList;
import com.extendedae_plus.network.SPacketEncodeFinished;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(PatternEncodingTermMenu.class)
public abstract class MixinEncodingMenu extends MEStorageMenu
        implements BridgeProviderList, BridgePlanToEncode, BridgeCtrlPressed {
    @Shadow
    @Final
    private RestrictedInputSlot encodedPatternSlot;
    @Shadow
    @Final
    private RestrictedInputSlot blankPatternSlot;
    @Shadow
    public abstract void encode();

    @Unique
    private Map<PatternContainerGroup, List<PatternContainer>> eaep$providerList;
    @Unique
    private boolean eaep$ctrlPressed = false;
    @Unique
    private boolean eaep$encodeActionDelayed = false;

    public MixinEncodingMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
            at = @At("TAIL"))
    private void onInit(MenuType<?> menuType,
                        int id,
                        Inventory ip,
                        IPatternTerminalMenuHost host,
                        boolean bindInventory,
                        CallbackInfo ci) {
        if (this.isClientSide()) return;
        this.eaep$providerList = PatternUploader.collectProvider(this);
    }

    @Override
    public Map<PatternContainerGroup, List<PatternContainer>> eaep$getProviderList() {
        if (this.eaep$providerList == null)
            this.eaep$providerList = new HashMap<>();
        return this.eaep$providerList;
    }

    @Override
    public void eaep$setCtrlPressed(boolean pressed) {
        this.eaep$ctrlPressed = pressed;
    }

    @Override
    public void eaep$plan() {
        this.eaep$encodeActionDelayed = true;
    }

    @Inject(method = "encode", at = @At("TAIL"))
    private void eaep$onEncode(CallbackInfo ci) {
        if (EAEPConfig.INDEPENDENT_UPLOADING_BUTTON.getAsBoolean()) return;
        if (this.isClientSide()) return;

        if (!this.eaep$ctrlPressed) return;
        this.eaep$ctrlPressed = false;

        var pattern = this.encodedPatternSlot.getItem();
        if (!PatternDetailsHelper.isEncodedPattern(pattern)) return;

        if (!(this.getPlayer() instanceof ServerPlayer player)) return;

        var flagMatrixUpload = PatternUploader.uploadToMatrix(player, this);
        if (flagMatrixUpload == null) {
            this.encodedPatternSlot.clearStack();
            var patternBlank = this.blankPatternSlot.getItem();
            if (patternBlank.isEmpty())
                this.blankPatternSlot.set(AEItems.BLANK_PATTERN.stack());
            else if (patternBlank.getCount() == patternBlank.getMaxStackSize())
                this.eaep$insertBlankPattern();
            else patternBlank.grow(1);
        } else if (!flagMatrixUpload) {
            PacketDistributor.sendToPlayer(player, SPacketEncodeFinished.INSTANCE);
        }
    }

    @Inject(method = "onSlotChange", at = @At("TAIL"))
    private void executeDelay(Slot s, CallbackInfo ci) {
        if (!this.eaep$encodeActionDelayed) return;
        this.eaep$encodeActionDelayed = false;
        this.encode();
    }

    @Inject(method = "encodePattern", at = @At("TAIL"), cancellable = true)
    private void onPatternEncode(CallbackInfoReturnable<ItemStack> cir) {
        var pattern = cir.getReturnValue();
        if (pattern.isEmpty()) return;

        pattern.set(ModDataComponents.DATA_ENCODER_PROFILE,
                new DataEncoderProfile(this.getPlayer().getGameProfile()));
        cir.setReturnValue(pattern);
    }

    @Unique
    private void eaep$insertBlankPattern() {
        var node = this.getGridNode();
        if (node != null) {
            var inv = node.getGrid().getStorageService().getInventory();

            if (inv.insert(AEItemKey.of(AEItems.BLANK_PATTERN),
                    1,
                    Actionable.SIMULATE,
                    this.getActionSource()) > 0) {
                inv.insert(AEItemKey.of(AEItems.BLANK_PATTERN),
                        1,
                        Actionable.MODULATE,
                        this.getActionSource());
                return;
            }
        }

        if (this.getPlayer().getInventory().add(AEItems.BLANK_PATTERN.stack()))
            return;

        this.getPlayer().drop(AEItems.BLANK_PATTERN.stack(), false);
    }
}
