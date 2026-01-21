package com.extendedae_plus.integration.jade;

import appeng.core.localization.InGameTooltip;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.integration.jade.helper.TooltipAppender;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.core.BlockPos;

public final class CommonTooltips {
    public static final TooltipAppender linkChannels = (accessor,
                                                        tooltip,
                                                        config,
                                                        data) -> {
        if (data.contains("used") && data.contains("max")) {
            int usedChannels = data.getInt("used");
            int maxChannels = data.getInt("max");
            if (maxChannels <= 0 || maxChannels == Integer.MAX_VALUE) {
                tooltip.add(InGameTooltip.Channels.text(usedChannels));
            } else {
                tooltip.add(InGameTooltip.ChannelsOf.text(usedChannels, maxChannels));
            }
        }
    };

    public static final TooltipAppender linkLabel = (accessor,
                                                     tooltip,
                                                     config,
                                                     data) -> {
        var builder = UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("label");

        if (data.contains("unset"))
            builder.addStr("unset");
        else if (data.contains("frequency"))
            builder.args(data.getLong("frequency"));
        else if (data.contains("label"))
            builder.args(data.getString("label"));

        tooltip.add(builder.build());
    };

    public static final TooltipAppender locationMaster = (accessor,
                                                          tooltip,
                                                          config,
                                                          data) -> {
        if (!data.contains("pos")) return;
        var pos = BlockPos.of(data.getLong("pos"));

        tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("master_location")
                .addStr(data.contains("name"), "custom_name")
                .args(pos.getX(), pos.getY(), pos.getZ(), data.getString("dim"))
                .build());
        if (data.contains("dim"))
            tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                    .item(ModItems.WIRELESS_TRANSCEIVER)
                    .addStr("master_location")
                    .addStr("dim")
                    .args(data.getString("dim"))
                    .build());
    };

    public static final TooltipAppender stateLocked = (accessor,
                                                       tooltip,
                                                       config,
                                                       data) -> {
        if (!data.contains("locked") || !data.getBoolean("locked")) return;
        tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("locked")
                .build());
    };

    public static final TooltipAppender infoPlacer = (accessor,
                                                      tooltip,
                                                      config,
                                                      data) -> {

        var placerName = data.contains("name") ? data.getString("name") : "";
        var placer = data.hasUUID("uuid") ? data.getUUID("uuid") : null;

        tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr(!placerName.isEmpty(), "name")
                .addStr(placerName.isEmpty() && placer != null, "id")
                .args(placerName, placer)
                .build());
    };
}
