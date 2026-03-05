package com.extendedae_plus.integration.impl.jade

import appeng.core.localization.InGameTooltip
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.integration.jade.TooltipAppender
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.core.BlockPos

object CommonTooltips {
    @JvmField
    val linkChannels = TooltipAppender { _, tooltip, _, data ->
        if (!data.contains("used") || !data.contains("max")) return@TooltipAppender

        val usedChannels = data.getInt("used")
        val maxChannels = data.getInt("max")

        if (maxChannels <= 0 || maxChannels == Int.MAX_VALUE)
            tooltip.add(InGameTooltip.Channels.text(usedChannels))
        else tooltip.add(InGameTooltip.ChannelsOf.text(usedChannels, maxChannels))
    }

    @JvmField
    val linkLabel = TooltipAppender { _, tooltip, _, data ->
        val builder = UtilKeyBuilder.of(Patterns.JadeInfo)
            .item(EAEPItems.WirelessTransceiver)
            .addStr("label")
        when {
            data.contains("unset") -> builder.addStr("unset")
            data.contains("frequency") -> builder.args(data.getLong("frequency"))
            data.contains("label") -> builder.args(data.getString("label"))
        }
        tooltip.add(builder.build())
    }

    @JvmField
    val locationMaster = TooltipAppender { _, tooltip, _, data ->
        if (!data.contains("pos")) return@TooltipAppender
        val pos = BlockPos.of(data.getLong("pos"))

        tooltip.add(
            UtilKeyBuilder.of(Patterns.JadeInfo)
                .item(EAEPItems.WirelessTransceiver)
                .addStr("master_location")
                .addStr(data.contains("name"), "custom_name")
                .args(pos.x, pos.y, pos.z, data.getString("dim"))
                .build()
        )
        if (data.contains("dim")) tooltip.add(
            UtilKeyBuilder.of(Patterns.JadeInfo)
                .item(EAEPItems.WirelessTransceiver)
                .addStr("master_location")
                .addStr("dim")
                .args(data.getString("dim"))
                .build()
        )
    }

    @JvmField
    val stateLocked = TooltipAppender { _, tooltip, _, data ->
        if (!data.contains("locked") || !data.getBoolean("locked")) return@TooltipAppender
        tooltip.add(
            UtilKeyBuilder.of(Patterns.JadeInfo)
                .item(EAEPItems.WirelessTransceiver)
                .addStr("locked")
                .build()
        )
    }

    @JvmField
    val infoPlacer = TooltipAppender { _, tooltip, _, data ->
            val placerName = data.getString("name")
            val placer = if (data.hasUUID("uuid")) data.getUUID("uuid") else null
            tooltip.add(
                UtilKeyBuilder.of(Patterns.JadeInfo)
                    .item(EAEPItems.WirelessTransceiver)
                    .addStr(!placerName.isEmpty(), "name")
                    .addStr(placerName.isEmpty() && placer != null, "id")
                    .args(placerName, placer.toString().substringBefore("-"))
                    .build()
            )
        }
}
