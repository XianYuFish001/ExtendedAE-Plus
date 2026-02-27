package com.extendedae_plus.network

import appeng.api.config.Settings
import appeng.api.config.YesNo
import appeng.api.networking.IGrid
import appeng.api.networking.IInWorldGridNodeHost
import appeng.blockentity.crafting.PatternProviderBlockEntity
import appeng.helpers.patternprovider.PatternProviderLogic
import appeng.helpers.patternprovider.PatternProviderLogicHost
import appeng.parts.crafting.PatternProviderPart
import appeng.util.EnumCycler
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.init.EAEPSettings
import com.extendedae_plus.common.registry.settings.StateSmartBlocking
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
import java.util.*

@FishNetworkPacket("provider_controller_operation")
@JvmRecord
data class CPacketProviderControllerOperation(
    val operationNormalBlocking: Operation,
    val operationSmartBlocking: Operation,
    val operationSmartDoubling: Operation,
    val gridPos: BlockPos,
    val clickedFace: Direction
) : CPacketGeneric {
    enum class Operation {
        NOOP, SET_TRUE, SET_FALSE, TOGGLE;

        companion object {
            val streamCodec: StreamCodec<RegistryFriendlyByteBuf, Operation> =
                NeoForgeStreamCodecs.enumCodec(Operation::class.java)
        }
    }

    override fun handleServer(player: ServerPlayer) {
        // 从控制方块实体的 AE2 节点确定 AE 网络上下文
        val grid = (player.serverLevel()
            .getBlockEntity(this.gridPos)
                as? IInWorldGridNodeHost)
            ?.getGridNode(this.clickedFace)
            ?.grid
            ?: return

        val affected = this.applyToAllProviders(grid)
        // 向发起玩家反馈影响数量，便于判断按钮是否生效
        player.displayClientMessage(
            UtilKeyBuilder.of(Patterns.Message)
                .item(EAEPItems.ControllerProvider)
                .addStr("global_switch")
                .args(affected)
                .build(), false
        )
    }

    /**
     * 遍历当前 ME 网络中所有样板供应器（方块、零件、第三方实现），并应用切换操作
     */
    private fun applyToAllProviders(grid: IGrid): Int {
        var affectedCount = 0
        // 用 Set 去重，因为同一个 Logic 实例可能被多种方式收集到
        val uniqueLogics: MutableSet<PatternProviderLogic> = HashSet<PatternProviderLogic>()

        // 1. AE2 原生的方块实体形式（Pattern Provider BlockEntity）
        collectLogicsFromMachineSet(
            grid.getMachines(PatternProviderBlockEntity::class.java),
            uniqueLogics
        )
        collectLogicsFromMachineSet(
            grid.getActiveMachines(PatternProviderBlockEntity::class.java),
            uniqueLogics
        )

        // 2. AE2 原生的电缆零件形式（Pattern Provider Part）
        collectLogicsFromMachineSet(
            grid.getMachines(PatternProviderPart::class.java),
            uniqueLogics
        )
        collectLogicsFromMachineSet(
            grid.getActiveMachines(PatternProviderPart::class.java),
            uniqueLogics
        )

        // 3. 任何实现了 PatternProviderLogicHost 接口的机器（包括 ExtendedAE 自己的扩展）
        collectLogicsFromMachineSet(
            grid.getMachines(PatternProviderLogicHost::class.java),
            uniqueLogics
        )
        collectLogicsFromMachineSet(
            grid.getActiveMachines(PatternProviderLogicHost::class.java),
            uniqueLogics
        )

        // 4. 兼容 ExtendedAE（glodblock）自己的 ExPatternProvider（因为 AE2 的 getMachines 只按精确类匹配接口会漏）
        collectByReflection(grid, uniqueLogics, "com.glodblock.github.extendedae.common.parts.PartExPatternProvider")
        collectByReflection(
            grid,
            uniqueLogics,
            "com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider"
        )

        // 真正执行切换
        for (logic in uniqueLogics) {
            if (this.applyOperationToLogic(logic)) {
                affectedCount++
            }
        }
        return affectedCount
    }

    /**
     * 对单个 PatternProviderLogic 应用本次包里携带的三种操作
     */
    private fun applyOperationToLogic(logic: PatternProviderLogic?): Boolean {
        if (logic == null) return false
        val configManager = logic.configManager ?: return false

        var anyChanged = false

        // 1. AE2 原生阻挡模式
        if (this.operationNormalBlocking != Operation.NOOP) {
            val prev = if (isBlockingModeEnabled(logic)) YesNo.YES else YesNo.NO
            val target: YesNo = calculateYesNoState(prev, this.operationNormalBlocking)
            configManager.putSetting(Settings.BLOCKING_MODE, target)
            anyChanged = prev != target
        }

        // 2. 高级阻挡模式
        if (this.operationSmartBlocking != Operation.NOOP) {
            val prev = configManager.getSetting(EAEPSettings.smartBlocking)
            val target: StateSmartBlocking = calculateTargetState<StateSmartBlocking>(
                prev,
                StateSmartBlocking.ENABLED,
                StateSmartBlocking.DISABLED,
                EnumSet.of(StateSmartBlocking.ENABLED, StateSmartBlocking.DISABLED),
                this.operationSmartBlocking
            )
            configManager.putSetting(EAEPSettings.smartBlocking, target)
            anyChanged = anyChanged or (prev != target)
        }

        // 3. 智能翻倍模式
        if (this.operationSmartDoubling != Operation.NOOP) {
            val prev = configManager.getSetting(EAEPSettings.smartDoubling)
            val target: YesNo = calculateYesNoState(prev, this.operationSmartDoubling)
            configManager.putSetting(EAEPSettings.smartDoubling, target)
            anyChanged = anyChanged or (prev != target)
        }

        // 有改动时保存并让 AE2 同步到客户端
        if (anyChanged) {
            try {
                logic.saveChanges()
            } catch (_: Throwable) {
            }
        }
        return anyChanged
    }

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, CPacketProviderControllerOperation> =
            StreamCodec.composite(
                Operation.streamCodec, CPacketProviderControllerOperation::operationNormalBlocking,
                Operation.streamCodec, CPacketProviderControllerOperation::operationSmartBlocking,
                Operation.streamCodec, CPacketProviderControllerOperation::operationSmartDoubling,
                BlockPos.STREAM_CODEC, CPacketProviderControllerOperation::gridPos,
                Direction.STREAM_CODEC, CPacketProviderControllerOperation::clickedFace,
                ::CPacketProviderControllerOperation
            )

        /**
         * 工具方法：把一个 Set 中的 Logic 加入去重集合
         */
        private fun collectLogicsFromMachineSet(machineSet: MutableSet<*>?, target: MutableSet<PatternProviderLogic>) {
            if (machineSet == null) return
            for (obj in machineSet) {
                addLogicIfPresent(target, obj)
            }
        }

        /**
         * 通过反射兼容第三方精确类（防止接口匹配漏掉）
         */
        private fun collectByReflection(grid: IGrid, target: MutableSet<PatternProviderLogic>, className: String) {
            try {
                val clazz = Class.forName(className)
                collectLogicsFromMachineSet(grid.getMachines(clazz), target)
                collectLogicsFromMachineSet(grid.getActiveMachines(clazz), target)
            } catch (_: Throwable) {
                // 如果类不存在（比如玩家没装 ExtendedAE）直接忽略
            }
        }

        /**
         * 从任意对象里尝试取出 PatternProviderLogic（兼容多种实现）
         */
        private fun addLogicIfPresent(target: MutableSet<PatternProviderLogic>, obj: Any?) {
            if (obj == null) return
            try {
                if (obj is PatternProviderLogicHost && obj.logic != null) {
                    target.add(obj.logic)
                    return
                }
                // 兜底反射调用 getLogic()
                val method = obj.javaClass.getMethod("getLogic")
                val result = method.invoke(obj)
                if (result is PatternProviderLogic) {
                    target.add(result)
                }
            } catch (_: Throwable) {
            }
        }

        /**
         * 根据当前状态和操作码计算目标状态
         */
        private fun <TEnum : Enum<TEnum>> calculateTargetState(
            prev: TEnum,
            trueValue: TEnum,
            falseValue: TEnum,
            validValues: EnumSet<TEnum>,
            operation: Operation
        ): TEnum {
            return when (operation) {
                Operation.SET_TRUE -> trueValue
                Operation.SET_FALSE -> falseValue
                Operation.TOGGLE -> EnumCycler.rotateEnum(prev, false, validValues)
                Operation.NOOP -> prev
            }
        }

        private fun calculateYesNoState(prev: YesNo, operation: Operation): YesNo {
            return calculateTargetState(
                prev,
                YesNo.YES,
                YesNo.NO,
                EnumSet.of(YesNo.YES, YesNo.NO),
                operation
            )
        }

        /**
         * 安全获取 AE2 原生阻挡模式状态（防止旧版本抛异常）
         */
        private fun isBlockingModeEnabled(logic: PatternProviderLogic) = try {
            logic.isBlocking
        } catch (_: Throwable) { false }
    }
}
