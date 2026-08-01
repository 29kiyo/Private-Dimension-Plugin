[日本語](README_JP.md)

# PrivateDimension

A private dimension plugin for Paper 1.21.5+

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
| 🧭 Safe Spawn Search  | Scans outward from the calculated spawn point to find safe ground, so custom structures with different floor heights still spawn players correctly |
| 🗂️ Custom NBT Structures | Drop your own `.nbt` structure file into `plugins/PrivateDimension/structures/` to replace the default plot layout |
| ⌨️ Tab Completion     | `/pd` subcommands and online player names (for `/pd give`) autocomplete in-game |

## Crafting Recipe

The item can be crafted using a normal crafting table.

### Dimension in a Bottle

![Dimension in a Bottle](https://cdn-ak.f.st-hatena.com/images/fotolife/C/Chuzume/20230105/20230105085556.png)

## Requirements

* Paper 1.21.5+
* Java 21+
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
| `/pd debug`         | Show plot-boundary debug status  | `privatedimension.debug` |

All subcommands and player names (for `/pd give`) support tab completion.

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
plot-floor-y: 64                  # Plot floor Y level (spawn Y = floor-y + 5 = 69, refined by safe-spawn search)
plot-height: 47                   # Structure height (Y size), used for plot boundary checks and safe-spawn search
structure-file: "plot48x48.nbt"   # Structure filename; see "Custom Structures" below
safe-spawn-search-radius: 8       # Horizontal search radius (blocks) for the safe-spawn search
safe-spawn-search-height: 12      # Vertical search range (blocks, up and down) for the safe-spawn search
pull-entity-limit: 10             # Maximum entities that can be transported
pull-entity-radius: 3.0           # Entity transport radius
enable-border-enforcement: true   # Force return when leaving plot boundaries
```

### Custom Structures (Custom NBT)

By default the plugin bundles `plot48x48.nbt`. To use your own layout:

1. Save your structure as a Vanilla Structure Block file (`.nbt`), e.g. via a Structure Block set to **Save** mode in-game.
2. Start the server once so `plugins/PrivateDimension/structures/` is created (a `README.txt` with instructions is generated there).
3. Copy your `.nbt` file into that folder using the same name as `structure-file` in `config.yml` (or update `structure-file` to match your filename).
4. If your structure's footprint or height differs from 48×48×47, update `plot-size` / `plot-height` accordingly.
5. Run `/pd reload` or restart the server.

Because the plugin performs a **safe-spawn search** (scanning outward from the calculated spawn point for solid ground with clear space above), players will spawn correctly on custom structures even if the floor height differs from the default — you generally don't need to hand-tune the spawn Y offset.

## Development / Local Testing (IntelliJ IDEA)

You can run a local Paper server directly from IntelliJ to test the plugin.

1. **One-time setup** — download a Paper server jar into `run/`:
   ```bash
   bash scripts/setup-test-server.sh
   ```
2. **Build the plugin and copy it into the test server:**
   ```bash
   bash scripts/build-and-copy.sh
   ```
   (Re-run this after every code change, then reload/restart the server.)
3. **Start the server:**
   - From IntelliJ: open **Run/Debug Configurations**, select **Paper Test Server** (already included in `.run/`, auto-detected by IntelliJ), and click ▶ Run.
   - Or from the terminal: `bash scripts/build-and-run.sh` (builds, copies, and starts in one step).
4. Connect with a Java Edition client to `localhost:25565` (the generated `server.properties` sets `online-mode=false` for easy offline testing — change this back if you need real account verification).
5. After editing code, stop the server (`stop` in the server console), re-run `scripts/build-and-copy.sh`, and start it again.

The `run/` folder (server jar, world data, logs) is git-ignored and safe to delete/regenerate at any time.

## Credits

PrivateDimension is based on the original **Private_Dimension** data pack created by Chuzume and is distributed in accordance with the terms of the original license.

## About Development

The code for this tool was written entirely by AI (Claude).
