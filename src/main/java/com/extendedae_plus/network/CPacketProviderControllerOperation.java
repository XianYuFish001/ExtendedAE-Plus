package com.extendedae_plus.network;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.parts.crafting.PatternProviderPart;
import appeng.util.EnumCycler;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.init.ModSettings;
import com.extendedae_plus.common.settings.StateSmartBlocking;
import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@EAEPNetworkPacket
public record CPacketProviderControllerOperation(
        Operation operationNormalBlocking,
        Operation operationSmartBlocking,
        Operation operationSmartDoubling,
        BlockPos gridPos,
        Direction clickedFace
) implements CPacketGeneric {
    public static final Type<CPacketProviderControllerOperation> TYPE = PacketGeneric.createType("provider_controller_operation");

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketProviderControllerOperation> STREAM_CODEC = StreamCodec.composite(
            Operation.STREAM_CODEC, CPacketProviderControllerOperation::operationNormalBlocking,
            Operation.STREAM_CODEC, CPacketProviderControllerOperation::operationSmartBlocking,
            Operation.STREAM_CODEC, CPacketProviderControllerOperation::operationSmartDoubling,
            BlockPos.STREAM_CODEC, CPacketProviderControllerOperation::gridPos,
            Direction.STREAM_CODEC, CPacketProviderControllerOperation::clickedFace,
            CPacketProviderControllerOperation::new
    );

    public enum Operation {
        NOOP, SET_TRUE, SET_FALSE, TOGGLE;

        public static final StreamCodec<RegistryFriendlyByteBuf, Operation> STREAM_CODEC =
                NeoForgeStreamCodecs.enumCodec(Operation.class);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // TODO Refactor
    @Override
    public void handleServer(ServerPlayer player) {
        // 从控制方块实体的 AE2 节点确定 AE 网络上下文
        var level = player.serverLevel();
        if (!(level.getBlockEntity(this.gridPos) instanceof IInWorldGridNodeHost host)) return;

        var node = host.getGridNode(this.clickedFace);
        if (node == null) return;

        IGrid grid = node.getGrid();
        if (grid == null) return;

        int affected = this.applyToAllProviders(grid);
        // 向发起玩家反馈影响数量，便于判断按钮是否生效
        player.displayClientMessage(UtilKeyBuilder.of(UtilKeyBuilder.message)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("global_switch")
                .args(affected)
                .build(), false);
    }

    /**
     * 遍历当前 ME 网络中所有样板供应器（方块、零件、第三方实现），并应用切换操作
     */
    private int applyToAllProviders(IGrid grid) {
        int affectedCount = 0;
        // 用 Set 去重，因为同一个 Logic 实例可能被多种方式收集到
        Set<PatternProviderLogic> uniqueLogics = new HashSet<>();

        // 1. AE2 原生的方块实体形式（Pattern Provider BlockEntity）
        collectLogicsFromMachineSet(grid.getMachines(PatternProviderBlockEntity.class), uniqueLogics);
        collectLogicsFromMachineSet(grid.getActiveMachines(PatternProviderBlockEntity.class), uniqueLogics);

        // 2. AE2 原生的电缆零件形式（Pattern Provider Part）
        collectLogicsFromMachineSet(grid.getMachines(PatternProviderPart.class), uniqueLogics);
        collectLogicsFromMachineSet(grid.getActiveMachines(PatternProviderPart.class), uniqueLogics);

        // 3. 任何实现了 PatternProviderLogicHost 接口的机器（包括 ExtendedAE 自己的扩展）
        collectLogicsFromMachineSet(grid.getMachines(PatternProviderLogicHost.class), uniqueLogics);
        collectLogicsFromMachineSet(grid.getActiveMachines(PatternProviderLogicHost.class), uniqueLogics);

        // 4. 兼容 ExtendedAE（glodblock）自己的 ExPatternProvider（因为 AE2 的 getMachines 只按精确类匹配接口会漏）
        collectByReflection(grid, uniqueLogics, "com.glodblock.github.extendedae.common.parts.PartExPatternProvider");
        collectByReflection(grid, uniqueLogics, "com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider");

        // 真正执行切换
        for (PatternProviderLogic logic : uniqueLogics) {
            if (this.applyOperationToLogic(logic)) {
                affectedCount++;
            }
        }
        return affectedCount;
    }

    /**
     * 工具方法：把一个 Set<? extends SomeMachine> 中的 Logic 加入去重集合
     */
    private static void collectLogicsFromMachineSet(Set<?> machineSet, Set<PatternProviderLogic> target) {
        if (machineSet == null) return;
        for (Object obj : machineSet) {
            addLogicIfPresent(target, obj);
        }
    }

    /**
     * 通过反射兼容第三方精确类（防止接口匹配漏掉）
     */
    private static void collectByReflection(IGrid grid, Set<PatternProviderLogic> target, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            collectLogicsFromMachineSet(grid.getMachines(clazz), target);
            collectLogicsFromMachineSet(grid.getActiveMachines(clazz), target);
        } catch (Throwable ignored) {
            // 如果类不存在（比如玩家没装 ExtendedAE）直接忽略
        }
    }

    /**
     * 从任意对象里尝试取出 PatternProviderLogic（兼容多种实现）
     */
    private static void addLogicIfPresent(Set<PatternProviderLogic> target, Object obj) {
        if (obj == null) return;
        try {
            if (obj instanceof PatternProviderLogicHost host && host.getLogic() != null) {
                target.add(host.getLogic());
                return;
            }
            // 兜底反射调用 getLogic()
            var method = obj.getClass().getMethod("getLogic");
            Object result = method.invoke(obj);
            if (result instanceof PatternProviderLogic logic) {
                target.add(logic);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 对单个 PatternProviderLogic 应用本次包里携带的三种操作
     */
    private boolean applyOperationToLogic(PatternProviderLogic logic) {
        if (logic == null) return false;
        var configManager = logic.getConfigManager();
        if (configManager == null) return false;

        boolean anyChanged = false;

        // 1. AE2 原生阻挡模式
        if (this.operationNormalBlocking != Operation.NOOP) {
            var prev = isBlockingModeEnabled(logic) ? YesNo.YES : YesNo.NO;
            var target = calculateYesNoState(prev, this.operationNormalBlocking);
            configManager.putSetting(Settings.BLOCKING_MODE, target);
            anyChanged |= (prev != target);
        }

        // 2. 高级阻挡模式
        if (this.operationSmartBlocking != Operation.NOOP) {
            var prev = configManager.getSetting(ModSettings.SMART_BLOCKING);
            var target = calculateTargetState(prev,
                    StateSmartBlocking.ENABLED,
                    StateSmartBlocking.DISABLED,
                    EnumSet.of(StateSmartBlocking.ENABLED, StateSmartBlocking.DISABLED),
                    this.operationSmartBlocking);
            configManager.putSetting(ModSettings.SMART_BLOCKING, target);
            anyChanged |= (prev != target);
        }

        // 3. 智能翻倍模式
        if (this.operationSmartDoubling != Operation.NOOP) {
            var prev = configManager.getSetting(ModSettings.SMART_DOUBLING);
            var target = calculateYesNoState(prev, this.operationSmartDoubling);
            configManager.putSetting(ModSettings.SMART_DOUBLING, target);
            anyChanged |= (prev != target);
        }

        // 有改动时保存并让 AE2 同步到客户端
        if (anyChanged) {
            try {
                logic.saveChanges();
            } catch (Throwable ignored) {
            }
        }
        return anyChanged;
    }

    /**
     * 根据当前状态和操作码计算目标状态
     */
    private static <TEnum extends Enum<TEnum>> TEnum
    calculateTargetState(TEnum prev, TEnum trueValue, TEnum falseValue, EnumSet<TEnum> validValues, Operation operation) {
        return switch (operation) {
            case SET_TRUE -> trueValue;
            case SET_FALSE -> falseValue;
            case TOGGLE -> EnumCycler.rotateEnum(prev, false, validValues);
            case NOOP -> prev;
        };
    }

    private static YesNo calculateYesNoState(YesNo prev, Operation operation) {
        return calculateTargetState(prev, YesNo.YES, YesNo.NO, EnumSet.of(YesNo.YES, YesNo.NO), operation);
    }

    /**
     * 安全获取 AE2 原生阻挡模式状态（防止旧版本抛异常）
     */
    private static boolean isBlockingModeEnabled(PatternProviderLogic logic) {
        try {
            return logic.isBlocking();
        } catch (Throwable t) {
            return false;
        }
    }
}
