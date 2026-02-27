package com.extendedae_plus.integration.impl.jade.helper

import net.minecraft.nbt.CompoundTag
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

fun interface TooltipAppender {
    fun add(accessor: BlockAccessor, tooltip: ITooltip, config: IPluginConfig, data: CompoundTag)

    fun add(name: String, accessor: BlockAccessor, tooltip: ITooltip, config: IPluginConfig) {
        require(
            accessor.serverData.get(name.lowercase()) is CompoundTag
        ) { "Unknown provider object " + name + " in " + accessor.block.name.string }
        this.add(accessor, tooltip, config, accessor.serverData.getCompound(name.lowercase()))
    }
}
