package com.extendedae_plus.integration.jade.wirelessTransceiver;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.integration.jade.CommonTooltips;
import com.extendedae_plus.integration.jade.TooltipAppender;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * 单文件聚合的 Jade 组件提供者，包含五个子组件常量，分别对应五个独立的开关/UID。
 */
public enum TooltipWirelessTransceiver implements IBlockComponentProvider {
    CHANNELS("channels", CommonTooltips.linkChannels),
    LABEL("label", CommonTooltips.linkLabel),
    MODE("master_mode", (accessor, tooltip, config, data) -> {
        if (data.contains("master_mode")) {
            boolean masterMode = data.getBoolean("master_mode");
            tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                    .item(ModItems.WIRELESS_TRANSCEIVER)
                    .addStr("mode")
                    .addStr(masterMode, "master", "slave")
                    .build());
        }
    }),
    MASTER_LOCATION("master_location", CommonTooltips.locationMaster),
    LOCKED("locked", CommonTooltips.stateLocked),
    PLACER("placer", CommonTooltips.infoPlacer);

    private final ResourceLocation uid;
    private final TooltipAppender adder;

    TooltipWirelessTransceiver(String path, TooltipAppender adder) {
        this.uid = ExtendedAEPlus.getLocation("wireless_transceiver." + path);
        this.adder = adder;
    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }

    @Override
    public final void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        this.adder.add(this.name(), accessor, tooltip, config);
    }
}


