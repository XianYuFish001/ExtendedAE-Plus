---
navigation:
  parent: introduction/index.md
  title: Ticking Card
  position: 6
  icon: extendedae_plus:card_ticking
categories:
  - extendedae_plus items
item_ids:
  - extendedae_plus:card_ticking
---

# Ticking Card

<Row>
  <ItemImage id="extendedae_plus:card_ticking" tag="{'extendedae_plus:data_ticking_card':{'multiplier':2,'max_multiplier':16}}" scale="3" />
  <ItemImage id="extendedae_plus:card_ticking" tag="{'extendedae_plus:data_ticking_card':{'multiplier':4,'max_multiplier':192}}" scale="3" />
  <ItemImage id="extendedae_plus:card_ticking" tag="{'extendedae_plus:data_ticking_card':{'multiplier':8,'max_multiplier':512}}" scale="3" />
  <ItemImage id="extendedae_plus:card_ticking" tag="{'extendedae_plus:data_ticking_card':{'multiplier':16,'max_multiplier':1024}}" scale="3" />
</Row>

The **Ticking Card** is an upgrade component that can be inserted into the <ItemLink id="extendedae_plus:ticker" /> to provide various levels of acceleration.  
The card’s tier directly determines the **maximum acceleration multiplier limit** that the Entity Accelerator can achieve.

## Card Tiers & Effects

An Entity Accelerator can hold up to **4 speed cards** simultaneously.  
The final acceleration multiplier equals the **product of all inserted cards’ base multipliers**, but it **cannot exceed** the **maximum speed limit** defined by the **lowest-tier card** installed.

| Card Type          | Base Multiplier | Maximum Speed Limit (per card) |
|:-------------------|:----------------|:-------------------------------|
| Ticking Card (x2)  | ×2              | ×16                            |
| Ticking Card (x4)  | ×4              | ×192                           |
| Ticking Card (x8)  | ×8              | ×512                           |
| Ticking Card (x16) | ×16             | ×1024                          |

> **Mixed Insertion Rule**:  
> When mixing cards of different tiers, the total multiplier is the product of all base multipliers, but the final result **cannot exceed** the **maximum speed limit** of the lowest-tier card installed.
>
> **Example**: Insert one **x2 card** and three **x4 cards**.
>
> - Calculated multiplier: 2 × 4³ = 128
> - Lowest-tier card: **x2**, with a limit of **×16**
> - **Final acceleration multiplier = ×16**

## Crafting Recipe
<RecipesFor id="extendedae_plus:card_ticking" />