package com.extendedae_plus.common.registry.dataComponent

import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.registry.block.wirelessTransceiver.TileWirelessTransceiver
import com.extendedae_plus.common.wireless.linkApi.Label
import com.fish.fishlib.util.extension.optional
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import java.util.*

@JvmRecord
data class DataChannelCard(val label: Label.Data, val owner: UUID?, val ownerName: String) {
    companion object {
        val codec: Codec<DataChannelCard> = RecordCodecBuilder.create({ instance ->
            instance.group(
                Label.Data.codec.fieldOf("data_label").forGetter(DataChannelCard::label),
                UUIDUtil.CODEC.lenientOptionalFieldOf("owner").forGetter(DataChannelCard::owner.optional()),
                Codec.STRING.fieldOf("owner_name").forGetter(DataChannelCard::ownerName)
            ).apply(instance) { data, owner, ownerName ->
                DataChannelCard(
                    data,
                    owner.orElse(null),
                    ownerName
                )
            }
        })

        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, DataChannelCard> = StreamCodec.composite(
            Label.Data.streamCodec, DataChannelCard::label,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), DataChannelCard::owner.optional(),
            ByteBufCodecs.STRING_UTF8, DataChannelCard::ownerName,
        ) { data, owner, ownerName ->
            DataChannelCard(
                data,
                owner.orElse(null),
                ownerName
            )
        }

        fun copyFromTransceiver(transceiver: TileWirelessTransceiver, card: ItemStack) {
            val label = transceiver.label.data
            val owner = transceiver.placer
            val ownerName = transceiver.placerName

            card.set(
                EAEPDataComponents.CardChannel,
                DataChannelCard(label, owner, ownerName)
            )
        }

        @JvmStatic
        fun setLabel(stack: ItemStack, label: Label.Data, changeOwner: Boolean) {
            val data = stack.get(EAEPDataComponents.CardChannel)

            val newData = if (data != null) DataChannelCard(
                label,
                if (changeOwner) label.placer else data.owner,
                if (changeOwner) label.placerName else data.ownerName
            ) else DataChannelCard(label, label.placer, label.placerName)

            stack.set(EAEPDataComponents.CardChannel, newData)
        }

        @JvmStatic
        fun getLabel(stack: ItemStack) =
            stack.get(EAEPDataComponents.CardChannel)?.label ?: Label.Data.Empty

        fun getOwner(stack: ItemStack): Pair<UUID?, String> {
            val data = stack.get(EAEPDataComponents.CardChannel)
            return (data?.owner) to (data?.ownerName ?: "")
        }

        @JvmStatic
        fun setOwner(stack: ItemStack, owner: UUID?, ownerName: String) {
            val data = stack.get(EAEPDataComponents.CardChannel)
            val label = data?.label ?: Label.Data.Empty
            stack.set(
                EAEPDataComponents.CardChannel,
                DataChannelCard(label, owner, ownerName)
            )
        }

        /**
         * 设置频道卡的所有者UUID
         */
        fun setOwnerUUID(stack: ItemStack, owner: UUID?) {
            val data = stack.get(EAEPDataComponents.CardChannel)

            stack.set(EAEPDataComponents.CardChannel, DataChannelCard(
                data?.label ?: Label.Data.Empty,
                owner,
                data?.ownerName ?: ""
            ))
        }

        /**
         * 获取频道卡的所有者UUID
         */
        @JvmStatic
        fun getOwnerUUID(stack: ItemStack) = stack.get(EAEPDataComponents.CardChannel)?.owner

        // 明明叫TeamName, 又会回退到Name, 这样叫会有歧义的
        /**
         * 设置团队名称（用于显示）
         */
        fun setOwnerName(stack: ItemStack, nameOwner: String) {
            val data = stack.get(EAEPDataComponents.CardChannel)

            stack.set(EAEPDataComponents.CardChannel, DataChannelCard(
                data?.label ?: Label.Data.Empty,
                data?.owner,
                nameOwner
            ))
        }

        /**
         * 获取团队名称
         */
        @JvmStatic
        fun getOwnerName(stack: ItemStack): String {
            val data = stack.get(EAEPDataComponents.CardChannel)
            return data?.ownerName ?: ""
        }

        /**
         * 清除所有者信息
         */
        @JvmStatic
        fun clearOwner(stack: ItemStack) {
            val data = stack.get(EAEPDataComponents.CardChannel)
            if (data != null) stack.set(
                EAEPDataComponents.CardChannel,
                DataChannelCard(data.label, null, "")
            )
        }
    }
}
