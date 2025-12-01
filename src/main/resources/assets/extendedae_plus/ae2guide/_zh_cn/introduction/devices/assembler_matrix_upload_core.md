---
navigation:
  parent: introduction/index.md
  title: 装配矩阵上传核心
  position: 8
  icon: extendedae_plus:assembler_matrix_upload_core
categories:
  - extendedae_plus devices
item_ids:
  - extendedae_plus:assembler_matrix_upload_core
---

# 装配矩阵上传核心

<BlockImage id="extendedae_plus:assembler_matrix_upload_core" scale="5" />

装配矩阵上传核心是一个功能模块，它为装配矩阵添加了从样板编码终端自动接收并上传样板的能力。

> [非官方] 当设置 `需要上传核心` 被关闭时, 样板上传不会再检测此核心

## 功能概述

将此核心安装到装配矩阵后，当您在样板编码终端中编码**合成样板**、**锻造台样板**或**切石机样板**时，终端将能够自动将这些样板上传至装配矩阵的存储中，无需手动转移。

> **注意**：此核心仅影响从**样板编码终端**到**装配矩阵**的**样板自动上传**功能。它不影响将处理样板上传到**样板供应器**的逻辑。