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
import appeng.util.ConfigManager;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.impl.pattern.PatternUploader;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.registry.dataComponent.DataEncoderProfile;
import com.extendedae_plus.common.registry.settings.ModeEncodingTransfer;
import com.extendedae_plus.mixin.bridge.BridgePlanToEncode;
import com.extendedae_plus.mixin.bridge.BridgeProviderList;
import com.extendedae_plus.mixin.impl.IOerMEStorage;
import com.extendedae_plus.network.SPacketEncodeFinished;
import com.extendedae_plus.network.SPacketProvidersInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
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
public abstract class MixinEncodingMenu extends MEStorageMenu implements BridgeProviderList, BridgePlanToEncode {
    @Unique
    private static final String eaep$actionUpload = "action_pattern_upload";

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
    private boolean eaep$encodeActionDelayed;
    @Unique
    private boolean eaep$uploadDelayed;

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
        this.registerClientAction(eaep$actionUpload, this::eaep$upload);

        if (this.isClientSide()) {
            if (!(this.getConfigManager() instanceof ConfigManager manager)) return;
            manager.registerSetting(ModSettings.modeTransfer, ModeEncodingTransfer.NONE);
            return;
        }
        this.eaep$providerList = PatternUploader.collectProvider(this);
    }

    @Override
    public Map<PatternContainerGroup, List<PatternContainer>> eaep$getProviderList() {
        if (this.eaep$providerList == null)
            this.eaep$providerList = new HashMap<>();
        return this.eaep$providerList;
    }

    @Override
    public void eaep$plan() {
        this.eaep$encodeActionDelayed = true;
    }

    @Override
    public boolean eaep$planned() {
        try {
            return this.eaep$encodeActionDelayed;
        } finally {
            this.eaep$encodeActionDelayed = false;
        }
    }

    @Inject(method = "encode", at = @At("RETURN"))
    private void onEncode(CallbackInfo ci) {
        if (this.isServerSide()) {
            this.eaep$fillBlankPattern(0);
            PacketDistributor.sendToPlayer((ServerPlayer) this.getPlayer(), SPacketEncodeFinished.INSTANCE);
            return;
        }

        if (EAEPConfig.independentUploadButton.getAsBoolean()) return;
        if (!Screen.hasControlDown()) return;

        this.eaep$uploadDelayed = true;
    }

    @Override
    public void eaep$execute() {
        if (this.isServerSide()) return;

        var pattern = this.encodedPatternSlot.getItem();
        if (pattern.isEmpty()) return;

        if (!this.eaep$uploadDelayed) return;
        this.eaep$uploadDelayed = false;

        if (!PatternDetailsHelper.isEncodedPattern(pattern)) return;

        this.sendClientAction(eaep$actionUpload);
    }

    @Inject(method = "encodePattern", at = @At("TAIL"), cancellable = true)
    private void onPatternEncode(CallbackInfoReturnable<ItemStack> cir) {
        var pattern = cir.getReturnValue();
        if (pattern == null || pattern.isEmpty()) return;

        pattern.set(ModDataComponents.DATA_ENCODER_PROFILE,
                new DataEncoderProfile(this.getPlayer().getGameProfile()));
        cir.setReturnValue(pattern);
    }

    @Unique
    private void eaep$upload() {
        var pattern = this.encodedPatternSlot.getItem();
        if (!PatternDetailsHelper.isEncodedPattern(pattern)) return;

        if (!(this.getPlayer() instanceof ServerPlayer player)) return;

        var flagMatrixUpload = PatternUploader.uploadToMatrix(player, this);
        if (flagMatrixUpload == null) {
            this.encodedPatternSlot.clearStack();
            this.eaep$fillBlankPattern(1);
        } else if (!flagMatrixUpload) {
            SPacketProvidersInfo.send(((ServerPlayer) this.getPlayer()), this);
        }
    }

    @Unique
    private void eaep$fillBlankPattern(int countExternal) {
        ItemStack patternBlank;
        int countExisting;
        int countKeep;
        if (this.blankPatternSlot.getItem().isEmpty()) {
            patternBlank = AEItems.BLANK_PATTERN.stack();
            this.blankPatternSlot.set(patternBlank);
            countExisting = 0;
        } else {
            patternBlank = this.blankPatternSlot.getItem();
            countExisting = patternBlank.getCount();
        }
        countKeep = patternBlank.getMaxStackSize() / 2;

        var countExtract = countKeep - countExisting - countExternal;
        if (countExtract == 0) return;

        if (patternBlank.isEmpty())
            this.blankPatternSlot.set(AEItems.BLANK_PATTERN.stack(countKeep));
        else patternBlank.setCount(countKeep);

        var node = this.getGridNode();
        if (node == null) return;
        var inv = node.getGrid().getStorageService().getInventory();

        IOerMEStorage ioer = countExtract > 0 ? inv::extract : inv::insert;
        // 再溢出就消失算了(
        var simulated = ioer.apply(AEItemKey.of(AEItems.BLANK_PATTERN),
                Math.abs(countExtract),
                Actionable.SIMULATE,
                this.getActionSource());
        if (simulated <= 0) {
            this.blankPatternSlot.getItem().grow(countExtract);
            return;
        }

        ioer.apply(AEItemKey.of(AEItems.BLANK_PATTERN),
                Math.abs(countExtract),
                Actionable.MODULATE,
                this.getActionSource());
    }
}
