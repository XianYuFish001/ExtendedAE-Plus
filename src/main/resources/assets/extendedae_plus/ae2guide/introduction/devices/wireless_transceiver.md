---
navigation:
  parent: introduction/index.md
  title: Wireless Transceiver
  position: 5
  icon: extendedae_plus:wireless_transceiver
categories:
  - extendedae_plus devices
item_ids:
  - extendedae_plus:wireless_transceiver
---

<GameScene zoom="7" background="transparent">
    <ImportStructure src="../../structure/wireless_transceiver.snbt" />
</GameScene>

# Wireless Transceiver

The **Wireless Transceiver** is an advanced wireless AE device that combines the convenience of wireless connectors with the cross-dimensional capabilities of Quantum Rings, allowing ME network channels to be transmitted wirelessly to multiple locations.

## Operating Mechanism

### Basic Concept
The Wireless Transceiver uses a **master-slave** architecture:
- **Master:** Connected to the source ME network providing the channel.
- **Slave:** Placed where the channel is needed, receiving it from a master. **One master can serve multiple slaves.**

### Cross-Dimensional Transmission
Supports cross-dimensional channel transmission.  
**Requirement:** Both master and slave chunks must remain loaded.

## Operation Guide

### Frequency Settings

- **[Right-Click]** Increase frequency.
- **[Shift + Right-Click]** Decrease frequency.
- **[Shift + Left-Click]** Open a menu to set frequency directly.
- **[Hold Wrench] & [Left-Click]** Lock/Unlock the transceiver.

> Transceivers are **Renamable**

### Ownership & Security (v1.4.4+)

Starting from version 1.4.4, the Wireless Transceiver supports ownership binding for enhanced security and channel isolation:
- **Default Ownership:** Transceivers placed before 1.4.4 are considered **public devices**.
- **Automatic Binding:** From 1.4.4 onward, newly placed transceivers automatically bind to the player who placed them.
- **FTB Teams Support:** Devices can be bound to FTB teams. **Transceivers from different teams or players cannot communicate even if frequencies match**, ensuring channel isolation.