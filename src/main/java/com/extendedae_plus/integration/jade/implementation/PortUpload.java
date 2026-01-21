package com.extendedae_plus.integration.jade.implementation;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockEntityUpload;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload;
import com.extendedae_plus.integration.jade.CommonProviders;
import com.extendedae_plus.integration.jade.CommonTooltips;
import com.extendedae_plus.integration.jade.helper.IObjectedProvider;
import com.extendedae_plus.integration.jade.helper.TooltipAppender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.function.BiConsumer;

public class PortUpload {
    public enum Provider implements IObjectedProvider<BlockAccessor> {
        LABEL(CommonProviders.linkLabel),
        CHANNELS(CommonProviders.linkChannels),
        MASTER_LOCATION(CommonProviders.locationMaster(accessor -> {
            if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return null;
            return blockEntity.getLabel();
        })),
        LOCKED(CommonProviders.stateLocked(BlockUpload.LOCKED)),
        PLACER(CommonProviders.infoPlacer(accessor -> {
            if (!(accessor.getBlockEntity() instanceof BlockEntityUpload blockEntity)) return null;
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
        MASTER_LOCATION("master_location", CommonTooltips.locationMaster),
        LOCKED("locked", CommonTooltips.stateLocked),
        PLACER("placer", CommonTooltips.infoPlacer);

        private final ResourceLocation uid;
        private final TooltipAppender appender;

        Tooltip(String path, TooltipAppender appender) {
            this.uid = ExtendedAEPlus.getLocation("port_upload." + path);
            this.appender = appender;
        }

        @Override
        public ResourceLocation getUid() {
            return this.uid;
        }

        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            this.appender.add(this.name(), blockAccessor, iTooltip, iPluginConfig);
        }
    }
}
