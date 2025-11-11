package com.extendedae_plus.mixin.impl;

import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeInventory;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.item.ChannelCardItem;
import com.extendedae_plus.common.wireless.WirelessSlaveLink;
import com.extendedae_plus.common.wireless.endpoint.GenericNodeEndpointImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;
import java.util.function.Supplier;

public class HolderChannelCardLink {
    private final IManagedGridNode mainNode;
    private WirelessSlaveLink link = null;

    public boolean needsInitialize = false;
    private long lastFrequency = 0L;
    
    private final Supplier<BlockEntity> hostBlockEntity;
    private final Supplier<IUpgradeInventory> upgradeInventories;
    private final Runnable saveHostChanged;

    public static final HolderChannelCardLink EMPTY = new HolderChannelCardLink(
            null, null, null, null) {
        @Override public void updateLinkStatus() {
        }
        @Override public boolean needsInitialize() {
            return false;
        }
    };

    public HolderChannelCardLink(IManagedGridNode mainNode,
                                 Supplier<BlockEntity> hostBlockEntity,
                                 Supplier<IUpgradeInventory> upgradeInventories,
                                 Runnable saveHostChanged) {
        this.mainNode = mainNode;
        this.hostBlockEntity = hostBlockEntity;
        this.upgradeInventories = upgradeInventories;
        this.saveHostChanged = saveHostChanged;
    }

    public void updateLinkStatus() {
        if (this.hostBlockEntity.get() != null
                && this.hostBlockEntity.get().getLevel() != null
                && this.hostBlockEntity.get().getLevel().isClientSide())
            return;
        if (this.mainNode == null) return;
        if (this.mainNode.getNode() == null) {
            this.needsInitialize = true;
            return;
        }

        long channel = 0L;
        boolean found = false;
        UUID placerID = null;

        IUpgradeInventory upgrades = this.upgradeInventories.get();
        if (!upgrades.isEmpty()) {
            for (ItemStack stack : upgrades) {
                if (stack.isEmpty() || !stack.is(ModItems.CHANNEL_CARD)) continue;

                channel = ChannelCardItem.getChannel(stack);
                placerID = ChannelCardItem.getOwnerUUID(stack);
                found = true;
                break;
            }
        }

        if (!found) {
            this.lastFrequency = 0;
            if (this.link != null) {
                this.link.setFrequency(0L);
                this.link.updateStatus();
            }
            this.saveHostChanged.run();
            // 唤醒节点，加速 AE2 感知到连接断开
            this.mainNode.ifPresent((grid, node) -> {
                try {
                    grid.getTickManager().wakeDevice(node);
                } catch (Throwable ignored) {
                }
            });
            this.needsInitialize = this.link == null
                    || this.link.isConnected();
            return;
        }

        if (this.link == null) {
            var endpoint = new GenericNodeEndpointImpl(this.hostBlockEntity, this.mainNode::getNode);
            this.link = new WirelessSlaveLink(endpoint);
        }

        if (this.lastFrequency != channel){
            this.lastFrequency = channel;
            this.link.setFrequency(channel);
            this.link.setPlacerId(placerID);
            this.link.updateStatus();
            this.saveHostChanged.run();
            this.mainNode.ifPresent((grid, node) -> {
                try {
                    grid.getTickManager().wakeDevice(node);
                } catch (Throwable ignored) {
                }
            });
        }

        this.needsInitialize = !this.link.isConnected();
        if (!this.link.isConnected()) {
            // 如果连接失败，唤醒设备以便稍后重试
            this.mainNode.ifPresent((grid, node) -> {
                try {
                    grid.getTickManager().wakeDevice(node);
                } catch (Throwable ignored) {
                }
            });
        }
    }

    public boolean needsInitialize() {
        return needsInitialize;
    }
}
