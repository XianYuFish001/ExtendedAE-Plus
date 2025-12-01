---
navigation:
  parent: introduction/index.md
  title: 频道卡
  position: 2
  icon: extendedae_plus:channel_card
categories:
  - extendedae_plus items
item_ids:
  - extendedae_plus:channel_card
---

# 频道卡

<ItemImage id="extendedae_plus:channel_card" scale="4" />

频道卡是一种功能升级卡，它将<ItemLink id="extendedae_plus:wireless_transceiver" />的从端功能浓缩为一张卡片。将其安装到支持的AE设备后，该设备即可直接连接到同频率的无线收发器主端，无需使用线缆。

## 功能与使用

### 基本功能
- **无线连接**：插入频道卡的AE设备会自动寻找并连接至**相同频率**的无线收发器主端，从而接入其提供的ME网络频道。
- **调整频率**：
    - **[右键]** 增加频率
    - **[Shift + 右键]** 减少频率
    - **[Shift + 左键]** 绑定/解绑所有者

## 工作流程

1.  确保已有一个设置好频率和模式的无线收发器**主端**接入源ME网络。
2.  手持频道卡，通过**Shift+左键**完成所有权绑定。
3.  手持频道卡**右键**或**Shift+右键**调整至所需频率。
4.  将频道卡安装到目标AE设备（如ME接口）中。
5.  该设备将自动连接到同频率且所有权匹配的无线收发器主端。

> **注意**：频道卡与无线收发器主端必须在**频率**和**所有权**（v1.4.4+）上都匹配，才能建立连接。