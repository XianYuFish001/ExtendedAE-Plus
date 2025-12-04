---
navigation:
  parent: introduction/index.md
  title: Assembler Matrix Upload Port
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

# Assembler Matrix Upload Port

The **Assembler Matrix Upload Port** is a functional module that adds the ability for an Assembler Matrix to automatically receive and upload patterns from the Pattern Encoding Terminal.

> **Tip:** If the config `NeedsUploadingPort` toggled false, the pattern upload will no longer detect this Port.

## Feature Overview

When installed in an Assembler Matrix, patterns created in the Pattern Encoding Terminal — including **crafting patterns**, **smelter patterns**, or **stonecutter patterns** — are automatically uploaded into the Assembler Matrix storage without manual transfer.

> **Note:** This Port only affects the **automatic upload of patterns from the Pattern Encoding Terminal to the Assembler Matrix**. It does not affect the logic for uploading processing patterns to **Pattern Providers**.
