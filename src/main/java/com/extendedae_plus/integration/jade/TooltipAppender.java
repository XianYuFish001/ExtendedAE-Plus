package com.extendedae_plus.integration.jade;

import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@FunctionalInterface
public interface TooltipAppender {
    void add(BlockAccessor accessor, ITooltip tooltip, IPluginConfig config, CompoundTag data);
}
