package com.extendedae_plus.integration.jade.implementation;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver;
import com.extendedae_plus.integration.jade.CommonProviders;
import com.extendedae_plus.integration.jade.CommonTooltips;
import com.extendedae_plus.integration.jade.helper.IObjectedProvider;
import com.extendedae_plus.integration.jade.helper.TooltipAppender;
import com.extendedae_plus.util.keyBuilder.Patterns;
import com.extendedae_plus.util.keyBuilder.UtilKeyBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.function.BiConsumer;

public class WirelessTransceiver {
    public enum Provider implements IObjectedProvider<BlockAccessor> {
        MODE((data, accessor) -> {
            var blockState = accessor.getBlockState();
            if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver)) return;
            data.putBoolean("master_mode", blockState.getValue(BlockWirelessTransceiver.MASTER_MODE));
        }),
        LABEL(CommonProviders.linkLabel),
        CHANNELS(CommonProviders.linkChannels),
        MASTER_LOCATION(CommonProviders.locationMaster(accessor -> {
            if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver blockEntity))
                return null;
            if (accessor.getBlockState().getValue(BlockWirelessTransceiver.MASTER_MODE)) return null;
            return blockEntity.getLabel();
        })),
        LOCKED(CommonProviders.stateLocked(BlockWirelessTransceiver.LOCKED)),
        PLACER(CommonProviders.infoPlacer(accessor -> {
            if (!(accessor.getBlockEntity() instanceof BlockEntityWirelessTransceiver blockEntity))
                return null;
            return blockEntity.getPlacer();
        }));

        private final BiConsumer<CompoundTag, BlockAccessor> provider;

        Provider(BiConsumer<CompoundTag, BlockAccessor> provider) {
            this.provider = provider;
        }

        @Override
        public BiConsumer<CompoundTag, BlockAccessor> getProvider() {
            return this.provider;
        }
    }

    public enum Tooltip implements IBlockComponentProvider {
        CHANNELS("channels", CommonTooltips.linkChannels),
        LABEL("label", CommonTooltips.linkLabel),
        MODE("master_mode", (accessor, tooltip, config, data) -> {
            if (data.contains("master_mode")) {
                boolean masterMode = data.getBoolean("master_mode");
                tooltip.add(UtilKeyBuilder.of(Patterns.jadeInfo)
                        .item(ModItems.WirelessTransceiver)
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

        Tooltip(String path, TooltipAppender adder) {
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
}
