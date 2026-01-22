package com.extendedae_plus.network;

import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.PlayerSource;
import com.extendedae_plus.common.impl.menuLocator.WirelessTerminalLocator;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@EAEPNetworkPacket("pick_from_network")
public record CPacketPickFromNetwork(BlockPos pos, Direction face, Vec3 hitLoc) implements CPacketGeneric {
    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketPickFromNetwork> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CPacketPickFromNetwork::pos,
            Direction.STREAM_CODEC, CPacketPickFromNetwork::face,
            ByteBufCodecs.fromCodec(Vec3.CODEC), CPacketPickFromNetwork::hitLoc,
            CPacketPickFromNetwork::new
    );

    @Override
    public void handleServer(ServerPlayer player) {
        if (player.isCreative()) return;

        var level = player.serverLevel();
        var state = level.getBlockState(this.pos);
        if (state.isAir()) return;

        var infoOptional = WirelessTerminalLocator.locate(player);
        if (infoOptional.isEmpty()) return;
        var info = infoOptional.get();

        var terminalStack = info.terminalStack();
        var terminal = info.terminal();
        if (terminalStack.isEmpty()) return;

        var gridOptional = info.grid();
        if (gridOptional.isEmpty()) return;
        var grid = gridOptional.get();

        double powerUsage = pullItem(player, state, grid);
        if (powerUsage > 0) terminal.usePower(player, powerUsage, info.terminalStack());

        player.containerMenu.broadcastChanges();
    }

    private double pullItem(ServerPlayer player, BlockState blockState, IGrid grid) {
        var hitResult = new BlockHitResult(this.hitLoc, this.face, this.pos, true);
        var picked = blockState.getCloneItemStack(hitResult, player.serverLevel(), this.pos, player);
        if (picked.isEmpty()) picked = blockState.getBlock().asItem().getDefaultInstance();
        if (picked.isEmpty()) return 0;

        IEnergyService energy = grid.getEnergyService();
        MEStorage storage = grid.getStorageService().getInventory();

        int pullCount = picked.getMaxStackSize();
        var mainHandItem = player.getMainHandItem();
        if (ItemStack.isSameItemSameComponents(mainHandItem, picked) &&
                mainHandItem.getCount() < pullCount)
            pullCount -= mainHandItem.getCount();

        long extracted = StorageHelper.poweredExtraction(
                energy, storage, AEItemKey.of(picked), pullCount, new PlayerSource(player));
        if (extracted <= 0) return 0;

        picked.setCount((int) extracted);
        if (mainHandItem.isEmpty()) {
            player.getInventory().setPickedItem(picked.copyAndClear());
        } else player.addItem(picked);

        if (!picked.isEmpty())
            StorageHelper.poweredInsert(energy, storage, AEItemKey.of(picked), picked.getCount(), new PlayerSource(player));
        return Math.max(0.5, (extracted - picked.getCount()) * 0.05);
    }
}
