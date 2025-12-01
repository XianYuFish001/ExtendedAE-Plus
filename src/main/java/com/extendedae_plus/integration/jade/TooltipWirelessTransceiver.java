package com.extendedae_plus.integration.jade;

import appeng.core.localization.InGameTooltip;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

/**
 * 单文件聚合的 Jade 组件提供者，包含五个子组件常量，分别对应五个独立的开关/UID。
 */
public enum TooltipWirelessTransceiver implements IBlockComponentProvider {
    CHANNELS("channels", (accessor, tooltip, config, data) -> {
        if (data.contains("usedChannels") && data.contains("maxChannels")) {
            int usedChannels = data.getInt("usedChannels");
            int maxChannels = data.getInt("maxChannels");
            // 参考AE2的显示方式
            if (maxChannels <= 0 || maxChannels == Integer.MAX_VALUE) {
                // 无限频道或未设置
                tooltip.add(InGameTooltip.Channels.text(usedChannels));
            } else {
                // 显示 "已使用/最大"
                tooltip.add(InGameTooltip.ChannelsOf.text(usedChannels, maxChannels));
            }
        }
    }),
    FREQUENCY("frequency", (accessor, tooltip, config, data) -> {
        if (data.contains("frequency")) {
            long frequency = data.getLong("frequency");
            tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                    .item(ModItems.WIRELESS_TRANSCEIVER)
                    .addStr("frequency")
                    .addStr(frequency == 0, "unset")
                    .args(frequency)
                    .build());
        }
    }),
    MODE("master_mode", (accessor, tooltip, config, data) -> {
        if (data.contains("masterMode")) {
            boolean masterMode = data.getBoolean("masterMode");
            tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                    .item(ModItems.WIRELESS_TRANSCEIVER)
                    .addStr("mode")
                    .addStr(masterMode, "master", "slave")
                    .build());
        }
    }),
    MASTER_LOCATION("master_location", (accessor, tooltip, config, data) -> {
        if (data.contains("masterMode") && !data.getBoolean("masterMode") && data.contains("masterPos")) {
            BlockPos pos = BlockPos.of(data.getLong("masterPos"));
            String dim = data.contains("masterDim") ? data.getString("masterDim") : "";
            String customName = data.contains("customName") ? data.getString("customName") : null;

            tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                    .item(ModItems.WIRELESS_TRANSCEIVER)
                    .addStr("master_location")
                    .addStr(customName != null, "custom_name")
                    .args(pos.getX(), pos.getY(), pos.getZ(), customName)
                    .build());
            if (!dim.isEmpty()) tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                    .item(ModItems.WIRELESS_TRANSCEIVER)
                    .addStr("master_location")
                    .addStr("dim")
                    .args(dim)
                    .build());
        }
    }),
    LOCKED("locked", (accessor, tooltip, config, data) -> {
        if (data.contains("locked") && data.getBoolean("locked"))
            tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                    .item(ModItems.WIRELESS_TRANSCEIVER)
                    .addStr("locked")
                    .build());
    }),
    PLACER("placer", (accessor, tooltip, config, data) -> {
        String placerName = data.contains("placerName") ? data.getString("placerName") : "";
        UUID placer = data.hasUUID("placer") ? data.getUUID("placer") : null;

        tooltip.add(UtilKeyBuilder.of(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr(!placerName.isEmpty(), "name")
                .addStr(placerName.isEmpty() && placer != null, "id")
                .args(placerName, placer)
                .build());
    });

    private final ResourceLocation uid;
    private final TooltipAdder adder;

    TooltipWirelessTransceiver(String path, TooltipAdder adder) {
        this.uid = ExtendedAEPlus.getLocation("wireless_transceiver." + path);
        this.adder = adder;
    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }

    @Override
    public final void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        this.adder.add(accessor, tooltip, config, data);
    }

    @FunctionalInterface
    private interface TooltipAdder {
        void add(BlockAccessor accessor, ITooltip tooltip, IPluginConfig config, CompoundTag data);
    }
}


