---
navigation:
  parent: introduction/index.md
  title: 装配矩阵上传接口
  position: 2
  icon: extendedae_plus:assembler_matrix_upload
categories:
  - extendedae_plus devices
item_ids:
  - extendedae_plus:assembler_matrix_upload
---

<GameScene zoom="3" background="transparent">
    <ImportStructure src="../../structure/port_upload.snbt" />
</GameScene>

# 装配矩阵上传接口

装配矩阵上传接口是一个功能模块, 可以安装至装配矩阵外表面(不包含边框)，它为装配矩阵添加了从样板编码终端自动接收并上传样板的能力。

> [非官方] 当设置 `需要上传接口` 被关闭时, 样板上传不会再检测此接口

## 功能概述

将此接口安装到装配矩阵后，当您在样板编码终端中编码**合成样板**、**锻造台样板**或**切石机样板**时，终端将能够自动将这些样板上传至装配矩阵的存储中，无需手动转移。

> **注意**：此接口仅影响从**样板编码终端**到**装配矩阵**的**样板自动上传**功能。它不影响将处理样板上传到**样板供应器**的逻辑。