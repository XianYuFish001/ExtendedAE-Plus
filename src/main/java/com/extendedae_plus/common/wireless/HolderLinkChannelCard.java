package com.extendedae_plus.common.wireless;

import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeInventory;
import com.extendedae_plus.common.init.ModDataComponents;
import com.extendedae_plus.common.registry.dataComponent.DataChannelCard;
import com.extendedae_plus.common.wireless.host.HostGeneric;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class HolderLinkChannelCard {
    private final IManagedGridNode mainNode;
    private LinkSlave linkSlave;

    private DataChannelCard lastData = null;

    private final Supplier<BlockEntity> getterBlockEntity;
    private final Supplier<IUpgradeInventory> getterUpgradeInventory;

    public static final HolderLinkChannelCard EMPTY = new HolderLinkChannelCard(
            null, null, null) {
        @Override public void onUpgradesChanged() {}
        @Override public void onTickingInitialize() {}
        @Override public boolean needsInitialize() {
            return false;
        }
    };

    public HolderLinkChannelCard(IManagedGridNode mainNode,
                                 Supplier<BlockEntity> getterBlockEntity,
                                 Supplier<IUpgradeInventory> getterUpgradeInventory) {
        this.mainNode = mainNode;
        this.getterBlockEntity = getterBlockEntity;
        this.getterUpgradeInventory = getterUpgradeInventory;
    }

    public void onUpgradesChanged() {
        var data = this.findChannelCard();

        if (data == null && this.lastData != null) {
            this.getOrCreateLink().onUnloadOrRemove();
            this.lastData = null;
            return;
        }

        if (data != null && this.lastData != data) {
            this.getOrCreateLink().updateInfo(data.label().pack(), data.owner());
            this.lastData = data;
        }
    }

    private @Nullable DataChannelCard findChannelCard() {
        var upgradesInv = this.getterUpgradeInventory.get();
        if (upgradesInv == null) return null;
        var data = new AtomicReference<DataChannelCard>();
        upgradesInv.forEach(card -> {
            if (data.get() == null && card.has(ModDataComponents.DATA_CHANNEL_CARD))
                data.set(card.get(ModDataComponents.DATA_CHANNEL_CARD));
        });
        return data.get();
    }

    public void onTickingInitialize() {
        this.getOrCreateLink().register();
    }

    public boolean needsInitialize() {
        if (this.lastData != null)
            return !this.getOrCreateLink().connected();
        else return false;
    }

    private LinkSlave getOrCreateLink() {
        if (this.linkSlave == null)
            this.linkSlave = new LinkSlave(new HostGeneric(this.getterBlockEntity, this.mainNode::getNode));
        return this.linkSlave;
    }
}
