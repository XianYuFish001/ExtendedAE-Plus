---
navigation:
  parent: introduction/index.md
  title: Ticker
  position: 4
  icon: extendedae_plus:ticker
categories:
  - extendedae_plus devices
item_ids:
  - extendedae_plus:ticker
---

<GameScene zoom="8" background="transparent">
    <ImportStructure src="../../structure/ticker.snbt" />
</GameScene>

# Ticker
Ticker 通过消耗网络中的能量，为面对的方块提供 Tick 加速，其功能与「时间之瓶」类似。

## 工作机制
1. **启用条件**：应用升级卡 **<ItemLink id="extendedae_plus:card_ticking" tag="{'extendedae_plus:data_ticking_card':{'multiplier':16,'max_multiplier':1024}}" />** 以累积倍数
2. **加速效果**：最高可为方块提供**1024倍**的加速倍率
3. **能量供应**：加速过程持续消耗AE网络能量.
4. **配置选项**：可通过配置文件调整基础能耗、设置黑名单，以及为特定配置独立的能量消耗倍率

> 当 Applied Flux 被安装时, 通过设置 `允许Ticker提取磁盘能量` 可以使 Ticker 能抽取存储在磁盘中的能量

## 能量消耗机制

### 能量消耗计算

Ticker的基础能量消耗由设置 `Ticker能量消耗基础值` 控制

令：
- \( E \) = 加速倍率
- \( r \) = 基础能量消耗（默认512）

**公式**
![formula_total_energy](../../picture/formula_ticker_total_energy.png)

### 能量卡消耗减免

安装能量卡可有效降低设备能耗，节能效果随卡数量增加而提升，但存在边际递减效应。

令 \( n \) = 安装的能量卡数量

**公式**
![formula_total_energy](../../picture/formula_ticker_card_reduction.png)

> 实际能耗可能因类型配置而有所调整
