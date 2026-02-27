package com.extendedae_plus.dataGen

import appeng.core.definitions.AEItems
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.init.EAEPBlocks
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.registry.block.EAEPCraftingUnit
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.ContainerDataGen
import com.fish.fishlib.util.keyBuilder.Patterns
import com.fish.fishlib.util.keyBuilder.toKeyPattern
import com.glodblock.github.extendedae.common.EAESingletons
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

class LangZH(output: PackOutput) : LanguageProvider(output, ExtendedAEPlus.MODID, "zh_cn") {
    override fun addTranslations() {
        ContainerDataGen.bind("zh_cn", this::add)

        this.addItem(EAEPItems.Ticker, "Ticker")
        this.addItem(EAEPItems.CellInfinity, "吞噬万籁的寂静")
        this.addItem(EAEPItems.ControllerProvider, "样板供应器管理工具")
        this.addItem(EAEPItems.CardChannel, "频道卡")
        this.addItem(EAEPItems.CardAutoCompletion, "自动完成卡")
        this.addItem(EAEPItems.PriorityTool, "优先级覆写卡")
        this.addItem(EAEPItems.CardTicking, "Ticking倍增卡")
        UtilKeyBuilder.dataGen(EAEPItems.CardTicking)
            .addStr("multiplier")
            .buildInto("Ticking倍增卡 %s倍")

        this.addBlock(EAEPBlocks.WirelessTransceiver, "无线收发器")
        this.addBlock(EAEPBlocks.PortUpload, "装配矩阵上传接口")
        this.addBlock(EAEPBlocks.CoreAdvancedCrafter, "装配矩阵高级合成核心")
        this.addBlock(EAEPBlocks.CoreAdvancedPattern, "装配矩阵高级样板核心")
        this.addBlock(EAEPBlocks.CoreAdvancedSpeed, "装配矩阵高级速度核心")
        EAEPCraftingUnit.entries.forEach {
            this.addBlock(it.block, "${it.acceleratorThreads}x并行处理单元")
        }

        UtilKeyBuilder.dataGen(Patterns.CreativeTab)
            .addStr("main")
            .buildInto("ExtendedAE Plus")

        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(EAEPItems.CardChannel)
            .addStr("label")
            .branch("unset", "频率: 未设置")
            .branch("set_to", "已设置频率到 %s")
            .branch("set_to.none", "频率已清空")
            .buildInto("频率: %s")
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(EAEPItems.CardChannel)
            .branch("name", "已绑定到: %s")
            .branch("id", $$"已绑定到 UUID{%2$s}")
            .buildInto("未绑定")
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(EAEPItems.CardTicking)
            .branch("multiplier", "倍速乘数: %s")
            .branch("max", "最高生效: %s")
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(EAEPItems.Ticker)
            .branch(
                "advanced_tip", """
                        Ticking倍增耗能计算公式:
                        §2§oTicker基础耗能 * ((2147483647 / Ticker基础耗能) ^ 0.1) ^ (log2(总加速倍率))
                        §f能量卡耗能减免计算公式:
                        §2§o0.9 * (0.5 / 0.9)^((能量卡数 - 1) / 7)
                        """
            )
            .buildInto(
                """
                        放入Ticking倍增卡以启用加速
                        最高可达 1024 倍速
                        加速将消耗 ME 网络能量，网络能量不足时无法加速
                        """
            )
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(EAEPItems.CellInfinity)
            .addStr("description")
            .branch("colored", "——方寸之间, 自有千寰")
            .buildInto("§6九重献祭, 终得虚空回响§r——觐见§8虚空之主Iava§r, 赐汝此物\n")
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(AEItems.PROCESSING_PATTERN.get())
            .addStr("encoder")
            .buildInto("由 %s 编码")
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .addStr("bom")
            .addStr("help")
            .buildInto(
                """
                        
                        ---------§aExtendedAE Plus§r---------
                        使用 §6[Ctrl + 中键]§r 点击一个节点,
                        EMI会自动编写并上传该节点配方的样板
                        """
            )
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(EAEPItems.CardAutoCompletion)
            .branch("advanced_tip", "NAE2 我们喜欢你口牙")
            .buildInto("还是顾名思义, 可以在供应器发配完成时自动取消合成任务")
        UtilKeyBuilder.dataGen(Patterns.Tooltip)
            .item(EAEPItems.PriorityTool)
            .buildInto("简单的工具, 可以覆写机器的优先级")

        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("state_ticker")
            .branch("blacklisted", "§c§l目标方块处在黑名单")
            .branch("enabled", "将对目标方块进行Tick加速")
            .branch("disabled", "不会对目标方块进行Tick加速")
            .buildInto("Ticker状态")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("smart_blocking")
            .branch("enabled", "对于同一种配方将不再阻挡(需要开启原版的阻挡模式)")
            .branch("disabled", "这么好的功能为什么不打开呢")
            .branch("disabled_by_super", "不建议在不打开原版阻挡时使用喵")
            .buildInto("智能阻挡")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("smart_doubling")
            .branch("enabled", "根据请求量对处理样板进行智能缩放")
            .branch("disabled", "按原始样板数量进行发配")
            .buildInto("智能翻倍")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("upload_button")
            .branch("auto_upload", "\n§a[Ctrl] §7上传样板")
            .buildInto("§7上传样板")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("toggle_slot_display")
            .branch("enabled", "隐藏槽位")
            .branch("disabled", "显示槽位")
            .buildInto("切换样板槽位显示")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("provider_list")
            .addStr("candidate_keywords")
            .buildInto("§f§l候选关键词")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("priority_tool")
            .branch("keep", "保持值不变")
            .branch("increment", "每次覆写值递增")
            .branch("decrement", "每次覆写值递减")
            .buildInto("覆写模式")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("recipe_alias")
            .branch("add", "添加映射")
            .branch("remove", "移除映射")
            .buildInto("别名操作")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("row_slots_visible")
            .branch("visible", "可见")
            .branch("invisible", "不可见")
            .buildInto("样板槽位可见性")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("label_link")
            .addStr("info_label")
            .branch("public", "公共频段")
            .buildInto("所有者: %s{%s}")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("label_link")
            .addStr("label_description")
            .branch("empty", "无简介")
            .buildInto("简介: ")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("label_type")
            .branch("frequency", "频率")
            .branch("label", "字符串标签")
            .buildInto("Link标签种类")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("label_mode")
            .branch("public", "公共频段")
            .branch("private", "私人/队伍频段")
            .buildInto("Link标签频段")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("label_add")
            .buildInto("注册Link标签")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("label_locked")
            .branch("locked", "已锁定")
            .branch("unlocked", "未锁定")
            .buildInto("锁定设备标签")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("transceiver_mode")
            .branch("master", "主端")
            .branch("slave", "从端")
            .buildInto("设备模式")
        UtilKeyBuilder.dataGen(Patterns.ScreenTooltip)
            .addStr("transfer_mode")
            .branch("none", "不更改")
            .branch("merge_adjacency", "合并相邻物品")
            .branch("independence", "不合并")
            .buildInto("处理配方合并模式")

        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("provider_list")
            .branch("remap_success", "[EAEP] 重载映射成功")
            .branch("remap_failed", "[EAEP] 重载映射失败")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("provider_list")
            .addStr("add_alias")
            .branch("empty_query", "[EAEP] 查询列表为空, 请先输入待映射配方关键词")
            .branch("empty_alias", "[EAEP] 别名为空, 请先输入待映射别名")
            .branch("success", "[EAEP] 别名映射{%s → %s} 添加成功")
            .branch("failed", "[EAEP] 别名映射{%s} 添加失败")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("provider_list")
            .addStr("delete_alias")
            .branch("empty_alias", "[EAEP] 别名为空, 请先输入待删除别名")
            .branch("success", "[EAEP] 别名映射{%s × %s} 删除成功")
            .branch("failed", "[EAEP] 别名映射{%s} 删除失败")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("pattern_uploading")
            .addStr("duplicate_pattern")
            .buildInto("[EAEP] 样板重复, 已取消上传")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("provider_to_upload")
            .branch("selected", "[EAEP] 样板供应器{%s} 已选择, 可快速上传样板")
            .branch("unset", "[EAEP] 未选择样板供应器, 请先进行选择再上传")
            .branch("failed", "[EAEP] 快速上传样板执行失败")
            .branch("invalid_pattern", "[EAEP] 非处理样板, 无法上传")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .item(EAEPItems.ControllerProvider)
            .addStr("global_switch")
            .buildInto("[EAEP] 全局设置已生效, 共影响 样板供应器x%s")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("opened_provider_info")
            .buildInto("[EAEP] 正在远程打开 样板供应器{位置[%s], 维度[%s]}")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("tips_mod_load")
            .addStr("confirm")
            .branch("hover", "点击将设置 \"DependencyTip\" 关闭")
            .branch("callback", "§a设置成功")
            .buildInto("\n§e[知道了, 不再提示我]")
        UtilKeyBuilder.dataGen(Patterns.Message)
            .addStr("tips_mod_load")
            .branch(
                "expandedae", """
                        §6[EAEP/DependencyTip]
                        §f将EAEP与ExpandedAE同时安装时, 可能导致以下及更多功能无法使用:§7
                          - 智能阻挡&倍增
                          - 单个超过16线程的并行处理器
                          - 样板数量快速修改
                          """
            )

        UtilKeyBuilder.dataGen(Patterns.ActionBar)
            .item(AEItems.CERTUS_QUARTZ_KNIFE.get())
            .addStr("block_name_coping")
            .branch("success", "已复制 方块/部件名{%s} 到剪贴板")
            .branch("failed", "复制 方块/部件名{%s} 失败")
        UtilKeyBuilder.dataGen(Patterns.ActionBar)
            .item(EAEPItems.CardChannel)
            .addStr("binding")
            .branch("clear", "已取消绑定")
            .buildInto("已绑定到: %s")

        UtilKeyBuilder.dataGen(Patterns.Screen)
            .item(EAEPItems.Ticker)
            .branch("enabled", "§3§l加速已启用")
            .branch("needs_energy", "§6§l网络能量不足")
            .branch("disabled", "§0§l加速被关闭")
            .branch("blacklisted", "§c§l目标方块处在黑名单")
            .branch("speed_multiplier", "加速倍率: %s")
            .branch("energy_cost", "能耗: %s/t")
            .branch("power_ratio", "耗能减免: %s")
            .branch("cost_multiplier", "额外消耗倍率: %s")
        UtilKeyBuilder.dataGen(Patterns.Screen)
            .addStr("provider_list")
            .branch("query", "输入关键词以搜索...")
            .branch("alias", "输入待映射别名...")
            .branch("add_alias", "添加映射")
            .branch("delete_alias", "删除映射")
            .buildInto("选择样板供应器以上传")
        UtilKeyBuilder.dataGen(Patterns.Screen)
            .item(EAEPItems.ControllerProvider)
            .branch("blocking", "切换阻挡模式")
            .branch("smart_blocking", "切换智能阻挡")
            .branch("smart_doubling", "切换智能翻倍")
            .branch("all_on", "全部开启")
            .branch("all_off", "全部关闭")
            .buildInto("样板供应器管理面板")
        UtilKeyBuilder.dataGen(Patterns.Screen)
            .item(EAESingletons.EX_PATTERN_PROVIDER.asItem())
            .addStr("pages")
            .buildInto("第 %s/%s 页")
        UtilKeyBuilder.dataGen(Patterns.Screen)
            .item(EAEPItems.PriorityTool)
            .buildInto("配置覆写值")
        UtilKeyBuilder.dataGen(Patterns.Screen)
            .addStr("stacks_rename")
            .buildInto("重命名")
        UtilKeyBuilder.dataGen(Patterns.Screen)
            .addStr("label_link")
            .branch("register", "注册Link标签")
            .branch("label_value", "Link标签")
            .branch("label_description", "简介")
            .buildInto("选择Link标签")

        UtilKeyBuilder.dataGen(Patterns.KeywordGroup)
            .addStr("workstations")
            .buildInto("§6关键词组 {§r配方: §l%s§6}")

        UtilKeyBuilder.dataGen(Patterns.Config)
            .branch("title", "ExtendedAE Plus配置")
            .branch("state_on", "开")
            .branch("state_off", "关")
            .branch("ae", "AE2配置")
            .branch("ticker", "Ticker配置")
            .branch("assembler_matrix", "装配矩阵配置")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("pageMultiplier")
            .branch(
                "tooltip", """
                        扩展样板供应器总槽位容量的倍率
                        基础为36，每页仍显示36格，倍率会增加总页数/总容量
                        建议范围 1-16
                        """
            )
            .buildInto("扩展样板供应器槽位倍率")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("overrideAE2WTPicking")
            .branch(
                "tooltip", """
                        是否覆盖AE2WT使用中键从终端选取方块的逻辑
                        开启后选取方块的数量将不被限制在32个
                        """
            )
            .buildInto("覆盖AE2WT选取")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("showEncoderPatternPlayer")
            .branch(
                "tooltip", """
                        是否显示样板编码玩家
                        开启后将在样板 Tooltip 上添加样板的编码玩家
                        """
            )
            .buildInto("显示样板编码玩家")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("patternTerminalShowSlotsDefault")
            .branch(
                "tooltip", """
                        样板终端默认是否显示槽位
                        影响进入界面时SlotsRow的默认可见性，仅影响客户端显示
                        """
            )
            .buildInto("样板终端默认显示槽位")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("independentUploadingButton")
            .branch("tooltip", "启用后, 在样板编码终端会出现一个独立的按钮用于上传样板")
            .buildInto("独立上传按钮")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("craftingPauseThreshold")
            .branch("tooltip", "值越大则AE构建合成计划过程中的 wait/notify 次数越少，提升吞吐但会降低调度响应性")
            .buildInto("AE合成计算暂停检查阈值")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("smartScalingMaxMultiplier")
            .branch(
                "tooltip", """
                        智能倍增的最大倍数（0 表示不限制）
                        此倍数是针对单次样板产出的放大倍数上限，用于限制一次推送中按倍增缩放的规模
                        """
            )
            .buildInto("智能倍增最大倍数")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("providerRoundRobinEnable")
            .branch(
                "tooltip", """
                        智能倍增时是否对样板供应器轮询分配
                        仅多个供应器有相同样板时生效，开启后请求会均分到所有可用供应器，关闭则全部分配给单一供应器
                        注意：所有相关供应器需开启智能倍增，否则可能失效
                        """
            )
            .buildInto("启用样板供应器轮询分配")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("tickerBaseCost")
            .buildInto("Ticker能量消耗基础值")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("tickerBlacklist")
            .branch(
                "tooltip", """
                        匹配的方块将不会被加速
                        允许匹配方块注册名与标签
                        例如 'mekanism:enrichment_chamber', '#c:storage_blocks/unobtainium'
                        """
            )
            .buildInto("Ticker黑名单")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("tickerExternalMultiplier")
            .branch(
                "tooltip", """
                        为某些方块设置额外能量倍率,
                        格式 '<entry>\[<multiplier>\]'
                        entry 允许匹配方块注册名与标签
                        例如 'mekanism:enrichment_chamber[1.14]', '#c:storage_blocks/unobtainium[5.14]'
                        """
            )
            .buildInto("Ticker额外消耗倍率")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("allowDiskEnergy")
            .branch(
                "tooltip", """
                        是否允许Ticker从磁盘提取能量
                        开启后，Ticker将优先尝试从磁盘提取能量
                        注意: 仅当Applied Flux模组存在时生效
                        """
            )
            .buildInto("允许Ticker提取磁盘能量")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("baseCoreCrafterThreads")
            .buildInto("高级合成核心基础线程数")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("coreCrafterThreadAmplification")
            .branch(
                "tooltip", """
                        每存在5速度增幅(具体见GuideME),
                        高级合成核心线程数的涨幅,
                        设置为0则不增长
                        """
            )
            .buildInto("高级合成核心线程涨幅")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("maximumCoreCrafterThreads")
            .branch("tooltip", "注意: 最大线程数不可小于基础线程数")
            .buildInto("高级合成核心最大线程数")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("corePatternSlotMultiplier")
            .buildInto("高级样板核心槽位倍率")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("needsUploadingPort")
            .branch("tooltip", "启用后, 样板只能被上传到装有上传接口的装配矩阵")
            .buildInto("需要装配矩阵上传接口")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("dependencyTips")
            .branch("tooltip", "控制EAEP是否在发现特殊Mod关系时进行提示")
            .buildInto("特殊Mod关系提示")
        UtilKeyBuilder.dataGen(Patterns.Config)
            .addStr("smartDoublingAdapt")
            .buildInto("倍增样板发配大小自适应 (WIP)")

        UtilKeyBuilder.dataGen(Patterns.JadeInfo)
            .item(EAEPItems.WirelessTransceiver)
            .addStr("label")
            .branch("unset", "Link标签: 未设置")
            .buildInto("Link标签: %s")
        UtilKeyBuilder.dataGen(Patterns.JadeInfo)
            .item(EAEPItems.WirelessTransceiver)
            .addStr("mode")
            .branch("master", "主模式")
            .branch("slave", "从模式")
        UtilKeyBuilder.dataGen(Patterns.JadeInfo)
            .item(EAEPItems.WirelessTransceiver)
            .addStr("master_location")
            .branch("custom_name", $$"主节点: %4$s{%1$s, %2$s, %3$s}")
            .buildInto("主节点: 无线收发器{%s, %s, %s}")
        UtilKeyBuilder.dataGen(Patterns.JadeInfo)
            .item(EAEPItems.WirelessTransceiver)
            .addStr("master_location")
            .addStr("dim")
            .buildInto("维度: %s")
        UtilKeyBuilder.dataGen(Patterns.JadeInfo)
            .item(EAEPItems.WirelessTransceiver)
            .addStr("locked")
            .buildInto("已锁定")
        UtilKeyBuilder.dataGen(Patterns.JadeInfo)
            .item(EAEPItems.WirelessTransceiver)
            .branch("name", "所有者: %s")
            .branch("id", $$"所有者{%2$s}")
            .buildInto("公共频段")

        UtilKeyBuilder.dataGen(Patterns.JadeConfig)
            .addStr("wireless_transceiver")
            .branch("channels", "无线收发器: 显示频道数")
            .branch("label", "无线收发器: 显示Link标签")
            .branch("master_mode", "无线收发器: 显示模式")
            .branch("master_location", "无线收发器: 显示主节点位置")
            .branch("locked", "无线收发器: 显示锁定状态")
            .branch("placer", "无线收发器: 显示所有者")

        UtilKeyBuilder.dataGen($$"group%2$s.name".toKeyPattern())
            .branch("pattern_provider", "ME样板供应器")
            .branch("storage", "ME存储总线")

        ContainerDataGen.destroy("zh_cn")
    }
}