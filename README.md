[日本語](README_JP.md)

# PrivateDimension

A private dimension plugin for Paper 26.1.2+

## About This Plugin

This plugin is a derivative work of **Private_Dimension** created by Chuzume.

PrivateDimension is a Paper/Spigot plugin reimplementation of the original data pack. All copyrights and other rights related to the original concept and assets belong to Chuzume.

Neither the developer nor the distributor of this plugin shall be held responsible for any issues, damages, or losses resulting from its installation or use.

## Original Work & References

* Creator: [@Chuzume](https://x.com/Chuzume)
* Repository: https://github.com/Chuzume/Private_Dimension
* Video: https://www.youtube.com/watch?v=NrwN3NJLuiA

## Overview

Using a **Dimension in a Bottle** allows players to enter their own private dimension.

Each dimension contains a dedicated 48×48 plot generated exclusively for its owner.

This plugin recreates the functionality of the original **Private_Dimension** data pack for Paper servers.

## Features

| Feature               | Description                                                                 |
| --------------------- | --------------------------------------------------------------------------- |
| 🌀 Dimension Travel   | Right-click a Dimension in a Bottle to enter your private dimension         |
| 🔙 Return System      | Use the item again inside the dimension to return to your previous location |
| 👥 Entity Transport   | Sneak + use to bring nearby friendly entities within a 3-block radius       |
| 🏠 48×48 Private Plot | Automatically assigns a dedicated 48×48 space to each player                |
| 🚫 Plot Boundary      | Players leaving their plot are automatically returned to the main world     |
| ☠️ Death Handling     | Dying inside the dimension respawns the player in the main world            |
| 📱 Geyser Support     | Compatible with both Java Edition and Bedrock Edition via Geyser            |

## Crafting Recipe

The item can be crafted using a normal crafting table.

### Dimension in a Bottle

![Dimension in a Bottle](https://cdn-ak.f.st-hatena.com/images/fotolife/C/Chuzume/20230105/20230105085556.png)

## Requirements

* Paper 26.1.2+
* Java 25+
* Geyser (optional, for Bedrock Edition support)

## Installation

1. Place `PrivateDimension-*.jar` into your `plugins/` folder.
2. Restart the server.
3. The `private_dimension` world will be generated automatically on startup.

## Commands

| Command             | Description                      | Permission               |
| ------------------- | -------------------------------- | ------------------------ |
| `/pd give [player]` | Give a Dimension in a Bottle     | `privatedimension.admin` |
| `/pd info`          | Show information about your plot | Everyone                 |
| `/pd reload`        | Reload the configuration         | `privatedimension.admin` |

## Permissions

| Permission Node          | Default | Description             |
| ------------------------ | ------- | ----------------------- |
| `privatedimension.use`   | true    | Allows use of the item  |
| `privatedimension.admin` | op      | Administrative commands |
| `privatedimension.debug` | op      | Ignore plot boundaries  |

## Configuration (config.yml)

```yaml
world-name: "private_dimension"   # Dimension world name
plot-size: 48                     # Plot size
plot-spacing: 128                 # Distance between plots
plot-floor-y: 64                  # Plot floor Y level (spawn Y = floor-y + 5 = 69)
pull-entity-limit: 10             # Maximum entities that can be transported
pull-entity-radius: 3.0           # Entity transport radius
enable-border-enforcement: true   # Force return when leaving plot boundaries
```

## Credits

PrivateDimension is based on the original **Private_Dimension** data pack created by Chuzume and is distributed in accordance with the terms of the original license.
