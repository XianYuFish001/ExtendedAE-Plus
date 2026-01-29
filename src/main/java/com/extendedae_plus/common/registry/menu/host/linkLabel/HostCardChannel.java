package com.extendedae_plus.common.registry.menu.host.linkLabel;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;
import com.extendedae_plus.common.registry.dataComponent.DataChannelCard;
import com.extendedae_plus.common.registry.item.upgradeCard.CardChannel;
import com.extendedae_plus.common.wireless.linkApi.Label;
import net.minecraft.world.entity.player.Player;

public class HostCardChannel extends ItemMenuHost<CardChannel> implements HostLabelLink {
    public HostCardChannel(CardChannel item, Player player, ItemMenuHostLocator locator) {
        super(item, player, locator);
    }

    @Override
    public Label.Data getLabelData() {
        return DataChannelCard.getLabel(this.getItemStack());
    }

    @Override
    public boolean setLabelData(Label.Data label, boolean force) {
        DataChannelCard.setLabel(this.getItemStack(), label, true);
        return true;
    }
}
