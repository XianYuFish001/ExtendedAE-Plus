package com.extendedae_plus.network;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEKey;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.service.CraftingService;
import appeng.menu.me.crafting.CraftingCPUMenu;
import com.extendedae_plus.mixin.core.ae2.accessor.PatternProviderLogicAccessor;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端从 CraftingCPUScreen 发送：鼠标下条目对应的 AEKey。
 * 服务端在当前打开的 CraftingCPUMenu 所属网络中，定位匹配该 AEKey 的样板供应器，
 * 尝试打开其目标机器的 GUI。
 */
@EAEPNetworkPacket("open_screen_crafting_node_machine")
public record CPacketOpenScreenCraftingNodeMachine(AEKey what) implements CPacketGeneric {
    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketOpenScreenCraftingNodeMachine> STREAM_CODEC = StreamCodec.composite(
            AEKey.STREAM_CODEC, CPacketOpenScreenCraftingNodeMachine::what,
            CPacketOpenScreenCraftingNodeMachine::new
    );

    @Override
    public void handleServer(ServerPlayer player) {
        Level level = player.serverLevel();
        if (!(player.containerMenu instanceof CraftingCPUMenu menu)) return;

        // 通过菜单 target（可能是 BlockEntity/Part/ItemHost）按 IActionHost 获取 Grid
        IGrid grid = null;
        Object target = (menu).getTarget();
        if (target instanceof IActionHost host && host.getActionableNode() != null)
            grid = host.getActionableNode().getGrid();
        if (grid == null) return;

        var cs = grid.getCraftingService();
        if (!(cs instanceof CraftingService craftingService)) return;

        // 根据 AEKey 找到可能的 Pattern, 遍历提供该样板的 Provider
        List<ICraftingProvider> providers = new ArrayList<>();
        craftingService.getCraftingFor(this.what).stream()
                        .map(craftingService::getProviders)
                        .forEach(iteration ->
                                iteration.forEach(providers::addLast));

        providers.forEach(provider -> {
            if (!(provider instanceof PatternProviderLogic ppl)) return;

            // 使用 accessor 获取 host（受保护字段通过 accessor 访问）
            PatternProviderLogicHost host = ((PatternProviderLogicAccessor) ppl).eap$host();
            if (host == null) return;
            var pbe = host.getBlockEntity();

            List<Direction> delayedBlocks = new ArrayList<>();

            // 尝试对邻居打开 GUI（优先通过 MenuProvider）
            // TODO Refactor
            for (Direction dir : host.getTargets()) {
                BlockPos targetPos = pbe.getBlockPos().relative(dir);

                var tbe = level.getBlockEntity(targetPos);
                if (tbe instanceof MenuProvider provider1) {
                    player.openMenu(provider1, targetPos);
                    return;
                }

                var tState = level.getBlockState(targetPos);
                var provider2 = tState.getMenuProvider(level, targetPos);
                if (provider2 != null) {
                    player.openMenu(provider2, targetPos);
                    return;
                }

                // awc这AE怎么这么坏啊😭😭😭
                if (tbe instanceof AEBaseBlockEntity) {
                    switch (tbe) {
                        case InterfaceBlockEntity ignored -> delayedBlocks.addFirst(dir);
                        case PatternProviderBlockEntity ignored -> delayedBlocks.addLast(dir);
                        case CableBusBlockEntity bus when bus.getCableBus().getPart(dir.getOpposite()) != null ->
                                delayedBlocks.addFirst(dir);
                        default -> tState.useWithoutItem(level, player,
                                new BlockHitResult(player.position(), dir.getOpposite(), targetPos, false));
                    }
                }

                if (tbe == null) continue;
                String blockEntityClassName = tbe.getClass().getName().toLowerCase();
                if (blockEntityClassName.contains("mekanism") && blockEntityClassName.contains("tile")) {
                    try {
                        Method methodOpenGui = tbe.getClass().getMethod("openGui", Player.class);
                        methodOpenGui.invoke(tbe, (Player) player);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
                    return;
                }
            }
            if (delayedBlocks.isEmpty()) return;
            BlockPos pos = pbe.getBlockPos().relative(delayedBlocks.getFirst());

            if (level.getBlockEntity(pos) instanceof CableBusBlockEntity bus) {
                IPart part = bus.getCableBus().getPart(delayedBlocks.getFirst().getOpposite());
                if (part != null) part.onUseWithoutItem(player, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            } else {
                level.getBlockState(pos).useWithoutItem(level, player, new BlockHitResult(
                        player.position(), delayedBlocks.getFirst().getOpposite(), pos, false));
            }
        });
    }
}
