package com.extendedae_plus.client.screen;

import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.menu.MenuProviderController;
import com.extendedae_plus.network.CPacketProviderControllerOperation;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ScreenProviderController extends AbstractContainerScreen<MenuProviderController> {
    private static final Component CUSTOM_TITLE =
            UtilKeyBuilder.of(UtilKeyBuilder.screen)
                    .item(ModItems.PROVIDER_CONTROLLER)
                    .build();
    public ScreenProviderController(MenuProviderController menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 240;
        this.imageHeight = 140;
    }

    @Override
    protected void init() {
        super.init();
        int w = 70;           // 按钮宽
        int h = 20;           // 按钮高
        int s = 8;            // 按钮间距
        int y = this.topPos + 28; // 第一行 Y
        // 计算三列按钮的左侧起点，使其在面板内水平居中
        int totalW3 = w * 3 + s * 2;
        int x = this.leftPos + (this.imageWidth - totalW3) / 2;

        // 行1：三个单项切换
        addRenderableWidget(Button.builder(UtilKeyBuilder.of(UtilKeyBuilder.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("blocking")
                .build(), b ->
                PacketDistributor.sendToServer(new CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.TOGGLE,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        this.menu.getBlockEntityPos(),
                        this.menu.getClickedFace()
                ))).bounds(x, y, w, h).build());

        addRenderableWidget(Button.builder(UtilKeyBuilder.of(UtilKeyBuilder.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("smart_blocking")
                .build(), b ->
                PacketDistributor.sendToServer(new CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.TOGGLE,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        this.menu.getBlockEntityPos(),
                        this.menu.getClickedFace()
                ))).bounds(x + w + s, y, w, h).build());

        addRenderableWidget(Button.builder(UtilKeyBuilder.of(UtilKeyBuilder.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("smart_doubling")
                .build(), b ->
                PacketDistributor.sendToServer(new CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.NOOP,
                        CPacketProviderControllerOperation.Operation.TOGGLE,
                        this.menu.getBlockEntityPos(),
                        this.menu.getClickedFace()
                ))).bounds(x + (w + s) * 2, y, w, h).build());

        // 行2：一键全开/全关
        int y2 = y + h + 12;
        // 第二行：两列按钮，总宽并居中
        int totalW2 = w * 2 + s;
        int x2 = this.leftPos + (this.imageWidth - totalW2) / 2;
        addRenderableWidget(Button.builder(UtilKeyBuilder.of(UtilKeyBuilder.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("all_on")
                .build(), b ->
                PacketDistributor.sendToServer(new CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.SET_TRUE,
                        CPacketProviderControllerOperation.Operation.SET_TRUE,
                        CPacketProviderControllerOperation.Operation.SET_TRUE,
                        this.menu.getBlockEntityPos(),
                        this.menu.getClickedFace()
                ))).bounds(x2, y2, w, h).build());

        addRenderableWidget(Button.builder(UtilKeyBuilder.of(UtilKeyBuilder.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("all_off")
                .build(), b ->
                PacketDistributor.sendToServer(new CPacketProviderControllerOperation(
                        CPacketProviderControllerOperation.Operation.SET_FALSE,
                        CPacketProviderControllerOperation.Operation.SET_FALSE,
                        CPacketProviderControllerOperation.Operation.SET_FALSE,
                        this.menu.getBlockEntityPos(),
                        this.menu.getClickedFace()
                ))).bounds(x2 + w + s, y2, w, h).build());
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        // 在按钮区域绘制一个半透明面板，提升可读性
        int pad = 6;
        int panelLeft = this.leftPos - pad;
        int panelTop = this.topPos - pad;
        int panelRight = this.leftPos + this.imageWidth + pad;
        int panelBottom = this.topPos + this.imageHeight + pad;
        gfx.fill(panelLeft, panelTop, panelRight, panelBottom, 0xA01E1E1E);
        // 边框
        gfx.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0x80FFFFFF);
        gfx.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0x80000000);
        gfx.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0x80FFFFFF);
        gfx.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0x80000000);
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);
        gfx.drawString(this.font, CUSTOM_TITLE, this.leftPos + 10, this.topPos + 8, 0xFFFFFF, false);
    }

    @Override
    protected void renderLabels(net.minecraft.client.gui.GuiGraphics gfx, int mouseX, int mouseY) {
        // 不绘制默认的玩家物品栏标题（例如“物品栏”），避免与自定义面板重叠
        // 标题已在 render() 中手动绘制
    }
}
