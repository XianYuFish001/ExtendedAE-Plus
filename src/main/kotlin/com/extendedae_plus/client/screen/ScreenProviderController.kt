package com.extendedae_plus.client.screen

import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.registry.menu.MenuProviderController
import com.extendedae_plus.network.CPacketProviderControllerOperation
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.network.PacketDistributor

class ScreenProviderController(
    menu: MenuProviderController, inv: Inventory, title: Component
) : AbstractContainerScreen<MenuProviderController>(menu, inv, title) {
    init {
        this.imageWidth = 240
        this.imageHeight = 140
    }

    override fun init() {
        super.init()
        val w = 70 // 按钮宽
        val h = 20 // 按钮高
        val s = 8 // 按钮间距
        val y = this.topPos + 28 // 第一行 Y
        // 计算三列按钮的左侧起点，使其在面板内水平居中
        val totalW3 = w * 3 + s * 2
        val x = this.leftPos + (this.imageWidth - totalW3) / 2

        // 行1：三个单项切换
        this.addRenderableWidget(
            Button.builder(
                UtilKeyBuilder.of(Patterns.Screen)
                    .item(EAEPItems.ControllerProvider)
                    .addStr("blocking")
                    .build()
            ) {
                PacketDistributor.sendToServer(
                    CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.TOGGLE,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        this.menu.blockEntityPos,
                        this.menu.clickedFace
                    )
                )
            }.bounds(x, y, w, h).build()
        )

        this.addRenderableWidget(
            Button.builder(
                UtilKeyBuilder.of(Patterns.Screen)
                    .item(EAEPItems.ControllerProvider)
                    .addStr("smart_blocking")
                    .build()
            ) {
                PacketDistributor.sendToServer(
                    CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.TOGGLE,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        this.menu.blockEntityPos,
                        this.menu.clickedFace
                    )
                )
            }.bounds(x + w + s, y, w, h).build()
        )

        addRenderableWidget(
            Button.builder(
                UtilKeyBuilder.of(Patterns.Screen)
                    .item(EAEPItems.ControllerProvider)
                    .addStr("smart_doubling")
                    .build()
            ) {
                PacketDistributor.sendToServer(
                    CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.TOGGLE,
                        this.menu.blockEntityPos,
                        this.menu.clickedFace
                    )
                )
            }.bounds(x + (w + s) * 2, y, w, h).build()
        )

        // 行2：一键全开/全关
        val y2 = y + h + 12
        // 第二行：两列按钮，总宽并居中
        val totalW2 = w * 2 + s
        val x2 = this.leftPos + (this.imageWidth - totalW2) / 2
        addRenderableWidget(
            Button.builder(
                UtilKeyBuilder.of(Patterns.Screen)
                    .item(EAEPItems.ControllerProvider)
                    .addStr("all_on")
                    .build()
            ) {
                PacketDistributor.sendToServer(
                    CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.SET_TRUE,
                        CPacketProviderControllerOperation.Operation.SET_TRUE,
                        CPacketProviderControllerOperation.Operation.SET_TRUE,
                        this.menu.blockEntityPos,
                        this.menu.clickedFace
                    )
                )
            }.bounds(x2, y2, w, h).build()
        )

        addRenderableWidget(
            Button.builder(
                UtilKeyBuilder.of(Patterns.Screen)
                    .item(EAEPItems.ControllerProvider)
                    .addStr("all_off")
                    .build()
            ) {
                PacketDistributor.sendToServer(
                    CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.SET_FALSE,
                        CPacketProviderControllerOperation.Operation.SET_FALSE,
                        CPacketProviderControllerOperation.Operation.SET_FALSE,
                        this.menu.blockEntityPos,
                        this.menu.clickedFace
                    )
                )
            }.bounds(x2 + w + s, y2, w, h).build()
        )
    }

    override fun renderBg(gfx: GuiGraphics, partialTicks: Float, mouseX: Int, mouseY: Int) {
        // 在按钮区域绘制一个半透明面板，提升可读性
        val pad = 6
        val panelLeft = this.leftPos - pad
        val panelTop = this.topPos - pad
        val panelRight = this.leftPos + this.imageWidth + pad
        val panelBottom = this.topPos + this.imageHeight + pad
        gfx.fill(panelLeft, panelTop, panelRight, panelBottom, -0x5fe1e1e2)
        // 边框
        gfx.fill(panelLeft, panelTop, panelRight, panelTop + 1, -0x7f000001)
        gfx.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, -0x80000000)
        gfx.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, -0x7f000001)
        gfx.fill(panelRight - 1, panelTop, panelRight, panelBottom, -0x80000000)
    }

    override fun render(gfx: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks)
        super.render(gfx, mouseX, mouseY, partialTicks)
        gfx.drawString(this.font, CUSTOM_TITLE, this.leftPos + 10, this.topPos + 8, 0xFFFFFF, false)
    }

    override fun renderLabels(gfx: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

    companion object {
        private val CUSTOM_TITLE = UtilKeyBuilder.of(Patterns.Screen)
            .item(EAEPItems.ControllerProvider)
            .build()
    }
}
