package com.extendedae_plus.dataGen;

import appeng.core.definitions.AEItems;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.init.ModBlocks;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.UtilGetKey;
import com.glodblock.github.extendedae.common.EAESingletons;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;

public class LangZH extends LanguageProvider {
    public LangZH(PackOutput output) {
        super(output, ExtendedAEPlus.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        UtilGetKey.bindTranslator("zh_cn", this::add);

        this.addItem(ModItems.ENTITY_TICKER_PART_ITEM, "实体加速器");
        this.addItem(ModItems.INFINITY_BIGINTEGER_CELL_ITEM, "§4吞§c噬§6万§e籁§a的§b寂§d静");
        this.addItem(ModItems.PROVIDER_CONTROLLER, "样板供应器管理工具");
        this.addItem(ModItems.CHANNEL_CARD, "频道卡");
        this.addItem(ModItems.ENTITY_SPEED_CARD, "实体加速卡");
        new UtilGetKey(ModItems.ENTITY_SPEED_CARD)
                .addStr("multiplier").buildInto("实体加速卡 %s倍");

        this.addBlock(ModBlocks.WIRELESS_TRANSCEIVER, "无线收发器");
        this.addBlock(ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE, "装配矩阵上传核心");
        Arrays.stream(EAEPCraftingUnitType.values()).forEach(type ->
                this.addBlock(type.getBlock(), type.getAcceleratorThreads() + "x并行处理单元"));

        new UtilGetKey(UtilGetKey.creativeTab)
                .addStr("main").buildInto("ExtendedAE Plus");

        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .addStr("frequency")
                .branch("unset", "频率: 未设置")
                .branch("set_to", "已设置频率到 %s")
                .branch("set_to.none", "频率已清空")
                .buildInto("频率: %s");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .branch("name", "已绑定到: %s")
                .branch("id", "已绑定到 UUID{%2$s}")
                .buildInto("未绑定");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.ENTITY_SPEED_CARD)
                .branch("multiplier", "倍速乘数: %s")
                .branch("max", "最高生效: %s");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.ENTITY_TICKER_PART_ITEM)
                .buildInto("""
                        放入实体加速卡以启用加速
                        最高可达 1024x 加速
                        加速将消耗 ME 网络能量，网络能量不足时无法加速""");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.INFINITY_BIGINTEGER_CELL_ITEM)
                .addStr("description")
                .buildInto("""
                        §6九重献祭, 终得虚空回响§r——觐见§8虚空之主Iava§r, 赐汝此物
                        §b——§4方§d寸§c之§e间§a, §6自§b有§5千§9寰""");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(AEItems.PROCESSING_PATTERN.get())
                .addStr("encoder")
                .buildInto("由 %s 编码");
        new UtilGetKey(UtilGetKey.tooltip)
                .addStr("bom")
                .addStr("help")
                .buildInto("""
                        \n---------§aExtendedAE Plus§r---------
                        使用 §6[Ctrl + 中键]§r 点击一个节点,
                        EMI会自动编写并上传该节点配方的样板""");

        new UtilGetKey(UtilGetKey.screenTooltip)
                .item(ModItems.ENTITY_TICKER_PART_ITEM)
                .branch("blacklist", "§c§l机器已被禁用")
                .branch("enabled", "已启用: 将加速目标方块实体的tick")
                .branch("disabled", "已关闭: 不会对目标方块实体进行加速");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("smart_blocking")
                .branch("enabled", "智能阻挡\n已启用：对于同一种配方将不再阻挡(需要开启原版的阻挡模式)")
                .branch("disabled", "智能阻挡\n已禁用：这么好的功能为什么不打开呢");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("smart_doubling")
                .branch("enabled", "智能翻倍\n已启用：根据请求量对处理样板进行智能缩放")
                .branch("disabled", "智能翻倍\n已禁用：按原始样板数量进行发配");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("upload_button")
                .branch("auto_upload", "\n§a[Ctrl] §7上传样板")
                .buildInto("§7上传样板");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("toggle_slot_display")
                .branch("enabled", "隐藏槽位")
                .branch("disabled", "显示槽位")
                .buildInto("切换样板槽位显示");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("provider_list")
                .addStr("candidate_keywords")
                .buildInto("§o§l候选关键词");

        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_list")
                .branch("remap_success", "[EAEP] 重载映射成功")
                .branch("remap_failed", "[EAEP] 重载映射失败");
        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_list")
                .addStr("add_alias")
                .branch("empty_query", "[EAEP] 查询列表为空, 请先输入待映射配方关键词")
                .branch("empty_alias", "[EAEP] 别名为空, 请先输入待映射别名")
                .branch("success", "[EAEP] 别名映射{%s → %s} 添加成功")
                .branch("failed", "[EAEP] 别名映射{%s} 添加失败");
        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_list")
                .addStr("delete_alias")
                .branch("empty_alias", "[EAEP] 别名为空, 请先输入待删除别名")
                .branch("success", "[EAEP] 别名映射{%s × %s} 删除成功")
                .branch("failed", "[EAEP] 别名映射{%s} 删除失败");
        new UtilGetKey(UtilGetKey.message)
                .addStr("pattern_uploading")
                .addStr("duplicate_pattern")
                .buildInto("[EAEP] 样板重复, 已取消上传");
        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_to_upload")
                .branch("selected", "[EAEP] 样板供应器{%s} 已选择, 可快速上传样板")
                .branch("unset", "[EAEP] 未选择样板供应器, 请先进行选择再上传")
                .branch("failed", "[EAEP] 快速上传样板执行失败")
                .branch("invalid_pattern", "[EAEP] 非处理样板, 无法上传");
        new UtilGetKey(UtilGetKey.message)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("global_switch")
                .buildInto("[EAEP] 全局设置已生效, 共影响 样板供应器x%s");
        new UtilGetKey(UtilGetKey.message)
                .addStr("pattern_scaling")
                .branch("mul", "[EAEP] 已放大样板至%s倍: [共%s, 成功%s, 失败%s]")
                .branch("div", "[EAEP] 已缩小样板到1/%s: [共%s, 成功%s, 失败%s]");
        new UtilGetKey(UtilGetKey.message)
                .addStr("opened_provider_info")
                .buildInto("[EAEP] 正在远程打开 样板供应器{位置[%s], 维度[%s]}");

        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .branch("switch_locked", "已锁定")
                .branch("switch_unlock", "已关闭锁定");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("locked")
                .buildInto("更改被取消: 收发器已锁定");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("mode")
                .branch("master", "已切换到 主模式")
                .branch("slave", "已切换到 从模式");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency")
                .branch("unset", "频率已清空")
                .buildInto("频率: %s");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(AEItems.CERTUS_QUARTZ_KNIFE.get())
                .addStr("block_name_coping")
                .branch("success", "已复制 方块/部件名{%s} 到剪贴板")
                .branch("failed", "复制 方块/部件名{%s} 失败");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.CHANNEL_CARD)
                .addStr("binding")
                .branch("clear", "已取消绑定")
                .buildInto("已绑定到: %s");

        new UtilGetKey(UtilGetKey.screen)
                .item(ModItems.ENTITY_TICKER_PART_ITEM)
                .branch("enabled", "§2§l机器运行中")
                .branch("blacklist", "§c§l机器已被禁用")
                .branch("needs_energy", "§6§l网络能量不足")
                .branch("speed", "当前加速倍率: %s")
                .branch("energy", "能耗: %s/t")
                .branch("power_ratio", "功耗比例: %s")
                .branch("multiplier", "额外消耗倍率: %s");
        new UtilGetKey(UtilGetKey.screen)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency_input")
                .branch("input_field", "输入以修改频率...")
                .branch("confirm", "确认")
                .branch("cancel", "取消")
                .buildInto("输入频率");
        new UtilGetKey(UtilGetKey.screen)
                .addStr("provider_list")
                .branch("query", "输入关键词以搜索...")
                .branch("alias", "输入待映射别名...")
                .branch("remap_aliases", "重载映射表")
                .branch("add_alias", "添加映射")
                .branch("delete_alias", "删除映射")
                .buildInto("选择样板供应器以上传");
        new UtilGetKey(UtilGetKey.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .branch("blocking", "切换阻挡模式")
                .branch("smart_blocking", "切换智能阻挡")
                .branch("smart_doubling", "切换智能翻倍")
                .branch("all_on", "全部开启")
                .branch("all_off", "全部关闭")
                .buildInto("样板供应器管理面板");
        new UtilGetKey(UtilGetKey.screen)
                .item(EAESingletons.EX_PATTERN_PROVIDER.asItem())
                .addStr("pages")
                .buildInto("第 %s/%s 页");

        new UtilGetKey(UtilGetKey.keywordGroup)
                .addStr("workstations")
                .buildInto("§6关键词组 {§r配方: §l%s§6}");

        new UtilGetKey(UtilGetKey.config)
                .branch("title", "ExtendedAE Plus配置")
                .branch("state_on", "开")
                .branch("state_off", "关")
                .branch("ae", "AE2配置")
                .branch("wireless", "无线连接配置")
                .branch("entitySpeedTicker", "实体加速器配置");
        new UtilGetKey(UtilGetKey.config)
                .addStr("pageMultiplier")
                .branch("tooltip", """
                        扩展样板供应器总槽位容量的倍率
                        基础为36，每页仍显示36格，倍率会增加总页数/总容量
                        建议范围 1-16""")
                .buildInto("扩展样板供应器槽位倍率");
        new UtilGetKey(UtilGetKey.config)
                .addStr("overrideAE2WTPicking")
                .branch("tooltip", """
                        是否覆盖AE2WT使用中键从终端选取方块的逻辑
                        开启后选取方块的数量将不被限制在32个""")
                .buildInto("覆盖AE2WT选取");
        new UtilGetKey(UtilGetKey.config)
                .addStr("showEncoderPatternPlayer")
                .branch("tooltip", """
                        是否显示样板编码玩家
                        开启后将在样板 Tooltip 上添加样板的编码玩家""")
                .buildInto("显示样板编码玩家");
        new UtilGetKey(UtilGetKey.config)
                .addStr("patternTerminalShowSlotsDefault")
                .branch("tooltip", """
                        样板终端默认是否显示槽位
                        影响进入界面时SlotsRow的默认可见性，仅影响客户端显示""")
                .buildInto("样板终端默认显示槽位");
        new UtilGetKey(UtilGetKey.config)
                .addStr("independentUploadingButton")
                .branch("tooltip", "启用后, 在样板编码终端会出现一个独立的按钮用于上传样板")
                .buildInto("独立上传按钮");
        new UtilGetKey(UtilGetKey.config)
                .addStr("craftingPauseThreshold")
                .branch("tooltip", "值越大则AE构建合成计划过程中的 wait/notify 次数越少，提升吞吐但会降低调度响应性")
                .buildInto("AE合成计算暂停检查阈值");
        new UtilGetKey(UtilGetKey.config)
                .addStr("smartScalingMaxMultiplier")
                .branch("tooltip", """
                        智能倍增的最大倍数（0 表示不限制）
                        此倍数是针对单次样板产出的放大倍数上限，用于限制一次推送中按倍增缩放的规模""")
                .buildInto("智能倍增最大倍数");
        new UtilGetKey(UtilGetKey.config)
                .addStr("providerRoundRobinEnable")
                .branch("tooltip", """
                        智能倍增时是否对样板供应器轮询分配
                        仅多个供应器有相同样板时生效，开启后请求会均分到所有可用供应器，关闭则全部分配给单一供应器
                        注意：所有相关供应器需开启智能倍增，否则可能失效""")
                .buildInto("启用样板供应器轮询分配");
        new UtilGetKey(UtilGetKey.config)
                .addStr("wirelessMaxRange")
                .branch("tooltip", """
                        无线收发器最大连接距离（单位：方块）
                        从端与主端的直线距离需小于等于该值才会建立连接。""")
                .buildInto("无线最大距离");
        new UtilGetKey(UtilGetKey.config)
                .addStr("wirelessCrossDimEnable")
                .branch("tooltip", """
                        是否允许无线收发器跨维度建立连接
                        开启后，从端可连接到不同维度的主端（忽略距离限制）""")
                .buildInto("无线收发器允许跨维度连接");
        new UtilGetKey(UtilGetKey.config)
                .addStr("entityTickerCost")
                .buildInto("实体加速器能量消耗基础值");
        new UtilGetKey(UtilGetKey.config)
                .addStr("entityTickerBlackList")
                .branch("tooltip", """
                        实体加速器黑名单：匹配的方块将不会被加速。支持通配符/正则
                        例如 'minecraft:chest', 'minecraft:*', 'mekanism:.*_factory'""")
                .buildInto("实体加速器黑名单");
        new UtilGetKey(UtilGetKey.config)
                .addStr("entityTickerMultipliers")
                .branch("tooltip", """
                        额外消耗倍率配置：为某些方块设置额外能量倍率,
                        格式 'modid:blockid multiplier'，支持通配符/正则匹配
                        例如 'mekanism:.*_factory 2x'""")
                .buildInto("实体加速器额外消耗倍率");
        new UtilGetKey(UtilGetKey.config)
                .addStr("prioritizeDiskEnergy")
                .branch("tooltip", """
                        是否优先从磁盘提取FE能量
                        开启后，将优先尝试从磁盘提取FE能量；反之优先消耗AE网络中的能量
                        注意: 仅当Applied Flux模组存在时生效""")
                .buildInto("优先从磁盘提取FE能量");
        new UtilGetKey(UtilGetKey.config)
                .addStr("needsUploadingCore")
                .branch("tooltip", "启用后, 样板只能被上传到装有上传核心的装配矩阵")
                .buildInto("需要装配矩阵上传核心");

        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency")
                .branch("unset", "频率: 未设置")
                .buildInto("频率: %s");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("mode")
                .branch("master", "主模式")
                .branch("slave", "从模式");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("master_location")
                .branch("custom_name", "主节点: %4$s{%1$s, %2$s, %3$s}")
                .buildInto("主节点: 无线收发器{%s, %s, %s}");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("master_location")
                .addStr("dim")
                .buildInto("维度: %s");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("locked")
                .buildInto("已锁定");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .branch("name", "所有者: %s")
                .branch("id", "所有者{%2$s}")
                .buildInto("公共模式");

        new UtilGetKey(UtilGetKey.jadeConfig)
                .addStr("wireless_transceiver")
                .branch("channels", "无线收发器: 显示频道数")
                .branch("frequency", "无线收发器: 显示频率")
                .branch("master_mode", "无线收发器: 显示模式")
                .branch("master_location", "无线收发器: 显示主节点位置")
                .branch("locked", "无线收发器: 显示锁定状态")
                .branch("placer", "无线收发器: 显示所有者");

        new UtilGetKey("group%2$s.name")
                .branch("pattern_provider", "ME样板供应器")
                .branch("storage", "ME存储总线");

        UtilGetKey.destroy("zh_cn");
    }
}
