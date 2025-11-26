package com.extendedae_plus.client.screen;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.util.Platform;
import com.extendedae_plus.common.impl.entitySpeed.PowerUtils;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.menu.EntitySpeedTickerMenu;
import com.extendedae_plus.network.CPacketToggleTickerState;
import com.extendedae_plus.util.UtilGetKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntitySpeedTickerScreen<C extends EntitySpeedTickerMenu> extends UpgradeableScreen<C> {
    private boolean eap$entitySpeedTickerEnabled = false;           // 本地缓存的加速开关状态
    private final SettingToggleButton<YesNo> eap$entitySpeedTickerToggle; // 加速开关按钮

    /**
     * 构造函数，初始化界面和控件。
     *
     * @param menu            实体加速器菜单
     * @param playerInventory 玩家背包
     * @param title           界面标题
     * @param style           界面样式
     */
    public EntitySpeedTickerScreen(EntitySpeedTickerMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super((C) menu, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit()); // 添加功率单位切换按钮
        this.eap$entitySpeedTickerEnabled = menu.getAccelerateEnabled();

        // 初始化加速开关按钮
        eap$entitySpeedTickerToggle = new SettingToggleButton<>(
                Settings.BLOCKING_MODE,
                this.eap$entitySpeedTickerEnabled ? YesNo.YES : YesNo.NO,
                (btn, backwards) -> {
                    // 不做本地切换，点击仅发送自定义C2S，显示由@GuiSync回传
                    PacketDistributor.sendToServer(CPacketToggleTickerState.INSTANCE);
                }
        ) {
            @Override
            public List<Component> getTooltipMessage() {
                if (menu.targetBlacklisted) {
                    return List.of(
                            new UtilGetKey(UtilGetKey.screenTooltip)
                                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                                    .addStr("blacklist")
                                    .build()
                    );
                }
                return List.of(
                        new UtilGetKey(UtilGetKey.screenTooltip)
                                .item(ModItems.ENTITY_TICKER_PART_ITEM)
                                .addStr(eap$entitySpeedTickerEnabled, "enabled", "disabled")
                                .build()
                );
            }

            @Override
            protected Icon getIcon() {
                if (menu.targetBlacklisted) return Icon.INVALID;
                return this.getCurrentValue() == YesNo.YES ? Icon.AUTO_EXPORT_ON : Icon.AUTO_EXPORT_OFF;
            }
        };
        eap$entitySpeedTickerToggle.set(this.eap$entitySpeedTickerEnabled ? YesNo.YES : YesNo.NO);
        this.addToLeftToolbar(eap$entitySpeedTickerToggle);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (eap$entitySpeedTickerToggle != null && menu != null) {
            eap$entitySpeedTickerEnabled = menu.getAccelerateEnabled();
            // 如果目标在黑名单，禁用按钮并显示关闭状态
            eap$entitySpeedTickerToggle.set(menu.targetBlacklisted ? YesNo.NO : (eap$entitySpeedTickerEnabled ? YesNo.YES : YesNo.NO));
            eap$entitySpeedTickerToggle.active = !menu.targetBlacklisted;
        }
        textData();
    }

    public void refreshGui() {
        textData();
    }

    /**
     * 更新界面文本内容，包括加速状态、速度、能耗和倍率。
     */
    private void textData() {
        Map<String, Component> textContents = new HashMap<>();
        if (getMenu().targetBlacklisted) {
            // 黑名单禁用时的默认显示
            // 你家被黑名单key是enable???真是不得不服😓
            // "screen.extendedae_plus.entity_speed_ticker.enable": "§c§l机器已被禁用"
            textContents.put("state", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("blacklist")
                    .build());
            textContents.put("speed", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("speed")
                    .args(0)
                    .build());
            textContents.put("energy", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("energy")
                    .args(Platform.formatPower(0.0, false))
                    .build());
            textContents.put("power_ratio", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("power_ratio")
                    .args(PowerUtils.formatPercentage(0.0))
                    .build());
            textContents.put("multiplier", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("multiplier")
                    .args(String.format("%.2fx", 0.0))
                    .build());
        } else {
            // 正常状态下显示实际数据
            int energyCardCount = getMenu().energyCardCount;
            int effectiveSpeed = getMenu().effectiveSpeed;
            double finalPower = PowerUtils.computeFinalPowerForProduct(effectiveSpeed, energyCardCount);
            double remainingRatio = PowerUtils.getRemainingRatio(energyCardCount);

            if (!menu.networkEnergySufficient)
                textContents.put("state", new UtilGetKey(UtilGetKey.screen)
                        .item(ModItems.ENTITY_TICKER_PART_ITEM)
                        .addStr("needs_energy")
                        .build());

            textContents.put("speed", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("speed")
                    .args(effectiveSpeed)
                    .build());
            textContents.put("energy", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("energy")
                    .args(Platform.formatPower(finalPower, false))
                    .build());
            textContents.put("power_ratio", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("power_ratio")
                    .args(PowerUtils.formatPercentage(remainingRatio))
                    .build());
            textContents.put("multiplier", new UtilGetKey(UtilGetKey.screen)
                    .item(ModItems.ENTITY_TICKER_PART_ITEM)
                    .addStr("multiplier")
                    .args(String.format("%.2fx", getMenu().multiplier))
                    .build());
        }
        textContents.forEach(this::setTextContent);
    }
}