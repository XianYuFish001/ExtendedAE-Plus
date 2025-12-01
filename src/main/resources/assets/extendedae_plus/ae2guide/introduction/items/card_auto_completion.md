---
navigation:
  parent: introduction/index.md
  title: Auto Completion Card
  position: 3
  icon: extendedae_plus:card_auto_completion
categories:
  - extendedae_plus items
item_ids:
  - extendedae_plus:card_auto_completion
---

# Auto Completion Card

<Row>
  <ItemImage id="extendedae_plus:card_auto_completion" scale="4" />
</Row>

The Auto Completion Card is a specialized upgrade for Pattern Providers that instantly completes the "final dispatch" for tasks where the products don't need to be returned to storage.

## Features and Effects

- **Auto-Complete**: Instantly marks a task as complete when the provider is about to dispatch the final batch and the recipe doesn't require returning products to the ME network.
- **Queue Release**: Frees up the crafting memory immediately without waiting for product return, allowing for subsequent scheduling.
- **Single Card Limit**: Only 1 card can be installed per Pattern Provider (including extended versions).

## Usage Scenarios

| Scenario | Effect |
| :--- | :--- |
| Multiblock Construction | Materials are automatically marked as delivered once dispatched to the target location, no need to wait for return confirmation |
| Manual Crafting Aid | Non-automatable materials (like Nether Stars, enchanted books, etc.) are dispatched to a chest near the crafting table, automatically completing the task after manual collection |
| Bulk Block Placement | Directly dispatches large quantities of building blocks to the hotbar, auto-completing after placement |
| Automated Production Line Supply | Automatically completes after replenishing consumables (like furnace fuel, experience bottles, etc.) for automated production lines |

## Crafting Recipe

<RecipesFor id="extendedae_plus:card_auto_completion" />
