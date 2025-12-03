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

The **Ticker** consumes energy from the AE2 network to accelerate block entities in the front block space, functioning similarly to a “Time in a bottle”.

## Operating Mechanism

1. **Activation Requirement:** The accelerator must contain at least one **<ItemLink id="extendedae_plus:ticking_card" tag="{'extendedae_plus:data_ticking_card':{'multiplier':16,'max_multiplier':1024}}" />** to enable acceleration.
2. **Acceleration Effect:** Can accelerate block entities up to **1024×**.
3. **Energy Supply:** Acceleration continuously consumes network energy.
    - If the mod *Applied Flux* installed, FE energy stored in network can be consumed to continue acceleration by toggle the config `AllowDiskEnergy` on.
4. **Configuration Options:** Base energy consumption, blacklist, and external energy multiplier can be adjusted via the config file.

## Energy Consumption Mechanism

### Base Energy Calculation

The energy consumption of the Entity Accelerator is determined by the base configuration value `baseCost` and the target acceleration multiplier.

Let:
- \( E \) = base energy config (default 512)
- \( r \) = total multiplier

**BaseCost formula:**
![formula_total_energy](../../picture/formula_ticker_total_energy.png)

### Energy Card Efficiency

Installing Energy Cards reduces power consumption. The effect increases with the number of cards but has diminishing returns.

Let \( n \) = number of installed energy cards

**CardRatio Formula:**  
![formula_card_reduction](../../picture/formula_ticker_card_reduction.png)

### Final Energy Calculation

**Final Power Formula:**

**TotalCost = BaseCost × CardRatio**

> Actual energy consumption may vary depending on entity-specific configuration.