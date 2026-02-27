package com.extendedae_plus.network

import appeng.api.networking.IGrid
import appeng.api.stacks.AEItemKey
import appeng.api.storage.StorageHelper
import appeng.me.helpers.PlayerSource
import com.extendedae_plus.common.impl.WirelessTerminalLocator
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.max

@FishNetworkPacket("pick_from_network")
@JvmRecord
data class CPacketPickFromNetwork(val pos: BlockPos, val face: Direction, val hitLoc: Vec3) : CPacketGeneric {
    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, CPacketPickFromNetwork> =
            StreamCodec.composite(
                BlockPos.STREAM_CODEC, CPacketPickFromNetwork::pos,
                Direction.STREAM_CODEC, CPacketPickFromNetwork::face,
                ByteBufCodecs.fromCodec(Vec3.CODEC), CPacketPickFromNetwork::hitLoc,
                ::CPacketPickFromNetwork
            )
    }

    override fun handleServer(player: ServerPlayer) {
        if (player.isCreative) return

        val level = player.serverLevel()
        val state = level.getBlockState(this.pos)
        if (state.isAir) return

        val info = WirelessTerminalLocator.locate(player) ?: return

        val terminalStack = info.terminalStack
        val terminal = info.terminal
        if (terminalStack.isEmpty) return

        val grid = info.grid() ?: return

        val powerUsage = pullItem(player, state, grid)
        if (powerUsage > 0) terminal.usePower(player, powerUsage, info.terminalStack)

        player.containerMenu.broadcastChanges()
    }

    private fun pullItem(player: ServerPlayer, blockState: BlockState, grid: IGrid): Double {
        val hitResult = BlockHitResult(this.hitLoc, this.face, this.pos, true)
        var picked = blockState.getCloneItemStack(hitResult, player.serverLevel(), this.pos, player)
        if (picked.isEmpty) picked = blockState.block.asItem().defaultInstance
        if (picked.isEmpty) return 0.0

        val energy = grid.energyService
        val storage = grid.storageService.inventory

        var pullCount = picked.maxStackSize
        val mainHandItem = player.mainHandItem
        if (ItemStack.isSameItemSameComponents(mainHandItem, picked) &&
            mainHandItem.count < pullCount
        ) pullCount -= mainHandItem.count

        val extracted = StorageHelper.poweredExtraction(
            energy, storage, AEItemKey.of(picked), pullCount.toLong(), PlayerSource(player)
        )
        if (extracted <= 0) return 0.0

        picked.count = extracted.toInt()
        if (mainHandItem.isEmpty) {
            player.getInventory().setPickedItem(picked.copyAndClear())
        } else player.addItem(picked)

        if (!picked.isEmpty) StorageHelper.poweredInsert(
            energy,
            storage,
            AEItemKey.of(picked),
            picked.count.toLong(),
            PlayerSource(player)
        )
        return max(0.5, (extracted - picked.count) * 0.05)
    }
}
