package com.extendedae_plus.common.registry.item

import appeng.api.networking.IInWorldGridNodeHost
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.network.CPacketProviderControllerOperation
import com.extendedae_plus.network.CPacketProviderControllerOperation.Operation
import com.extendedae_plus.util.UtilKeyBuilder
import com.extendedae_plus.util.UtilTextComponent
import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToServer
import com.fish.fishlib.util.keyBuilder.Patterns
import com.fish.fishlib.util.keyBuilder.containerComponent
import com.fish.fishlib.util.keyBuilder.newContainerComponent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

class ItemProviderController : Item(Properties().stacksTo(1)) {
    override fun onItemUseFirst(stack: ItemStack, context: UseOnContext): InteractionResult {
        val level = context.level
        if (!level.isClientSide()) return InteractionResult.sidedSuccess(false)

        val player = context.player ?: return super.useOn(context)

        val tileRaw = level.getBlockEntity(context.clickedPos)
        if (tileRaw is IInWorldGridNodeHost) this.cui(stack, context)?.let {
            player.displayClientMessage(it, false)
        }
        return InteractionResult.SUCCESS
    }

    // 我们有最好的函数式cui构造😋
    @OnlyIn(Dist.CLIENT)
    private fun cui(stack: ItemStack, context: UseOnContext) = UtilKeyBuilder.of(Patterns.Message)
        .item(EAEPItems.ControllerProvider)
        .addStr("cui")
        .newContainerComponent()
        .args(stack.displayName)
        .buildInto("title") {
            it.withStyle {
                it.withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        UtilKeyBuilder.of(Patterns.Message)
                            .item(EAEPItems.ControllerProvider)
                            .addStr("cui")
                            .addStr("title")
                            .addStr("tip")
                            .build()
                    )
                )
            }
        }
        .newLine()
        .section("button") {
            it
                .section("blocking") {
                    it
                        .buildInto("normal", context.send(blockingNormal = Operation.TOGGLE))
                        .buildInto("smart", context.send(blockingSmart = Operation.TOGGLE))
                }
                .buildInto("doubling", context.send(doubling = Operation.TOGGLE))
                .newLine()
                .section("of_all") {
                    it
                        .buildInto(
                            "on", context.send(
                                Operation.SET_TRUE,
                                Operation.SET_TRUE,
                                Operation.SET_TRUE,
                                ChatFormatting.GOLD
                            )
                        ).buildInto(
                            "off", context.send(
                                Operation.SET_FALSE,
                                Operation.SET_FALSE,
                                Operation.SET_FALSE,
                                ChatFormatting.GOLD
                            )
                        )
                }
        }
        .newLine()
        .append(Component.literal("=".repeat(8 * 2 + stack.displayName.string.length)))
//        .buildInto("end") {
//            it.withStyle {
//                it.withHoverEvent(
//                    HoverEvent(
//                        HoverEvent.Action.SHOW_TEXT,
//                        UtilKeyBuilder.of(EAEPItems.ControllerProvider)
//                            .addStr("cui")
//                            .addStr("end")
//                            .addStr("tip")
//                            .build()
//                    )
//                )
//            }
//        }
        .containerComponent

    @OnlyIn(Dist.CLIENT)
    private fun UseOnContext.send(
        blockingNormal: Operation = Operation.NOOP,
        blockingSmart: Operation = Operation.NOOP,
        doubling: Operation = Operation.NOOP,
        color: ChatFormatting = ChatFormatting.GREEN
    ) = { component: MutableComponent ->
        component.withStyle {
            it.withClickEvent(UtilTextComponent.ClickEventCustomizable {
                CPacketProviderControllerOperation(
                    blockingNormal,
                    blockingSmart,
                    doubling,
                    this.clickedPos,
                    this.clickedFace
                ).sendToServer()
            }).withColor(color)
        }
    }
}
