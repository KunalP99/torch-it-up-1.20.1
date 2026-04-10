# Torch Placer

A Fabric mod for Minecraft 1.20.1 that automatically places torches in dark areas around you as you explore. Supports vanilla torches and 11 wood-variant torches, each with its own custom texture.

## Requirements

- Minecraft 1.20.1
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.18.6+
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.92.7+1.20.1

## How It Works

When enabled, the mod scans the blocks around you every 2 seconds. If it finds a spot that is darker than your configured light threshold, it will automatically take a torch from your inventory and place it there — prioritising side walls, then the front wall, then the floor.

The mod checks your inventory in order and uses the first torch it finds — vanilla or any wood variant. No torches in your inventory means no placement.

## Wood-Variant Torches

In addition to the vanilla torch, the mod adds 11 wood-typed torches — one for each wood type in the game. Each has its own appearance and is treated identically to a vanilla torch for auto-placement purposes.

| Torch | Crafting ingredients |
|-------|---------------------|
| Oak Torch | Oak Planks + Torch |
| Spruce Torch | Spruce Planks + Torch |
| Birch Torch | Birch Planks + Torch |
| Jungle Torch | Jungle Planks + Torch |
| Acacia Torch | Acacia Planks + Torch |
| Dark Oak Torch | Dark Oak Planks + Torch |
| Mangrove Torch | Mangrove Planks + Torch |
| Cherry Torch | Cherry Planks + Torch |
| Bamboo Torch | Bamboo Planks + Torch |
| Crimson Torch | Crimson Planks + Torch |
| Warped Torch | Warped Planks + Torch |

### Crafting

Each wood torch is crafted in any crafting grid (including the 2×2 inventory grid):

```
[ Plank ]
[ Torch ]
```

Place the matching wood plank directly above a vanilla torch. Yields 1 wood torch.

All wood torches emit the same light level as a vanilla torch and can be placed on floors and walls the same way.

## Keybindings

| Key | Action | Default |
|-----|--------|---------|
| `G` | Toggle auto-torch on/off | G |
| *(unbound)* | Open settings screen | — |

You can rebind either key in **Options → Controls → Torch Placer**.

When you toggle the mod, a message appears above your hotbar confirming the new state (**Auto Torch: ON** / **Auto Torch: OFF**).

## Settings Screen

Open the settings screen with your bound key (or via the Controls menu). Changes take effect immediately after clicking **Save**.

| Setting | Description | Range | Default |
|---------|-------------|-------|---------|
| **Light Threshold** | Places a torch when block light is at or below this value | 0 – 14 | 7 |
| **Placement** | Which surfaces torches can be placed on | Walls & Floor / Walls Only / Floor Only | Walls & Floor |
| **Scan Radius** | How many blocks away from you to scan | 3 – 10 | 5 |

### Light Threshold Guide

| Threshold | When torches are placed |
|-----------|------------------------|
| 0 | Only in complete darkness |
| 7 (default) | Comfortably lit caves — hostile mobs cannot spawn at light level 1+ in 1.20.1, so 7 gives a generous safety margin |
| 14 | Almost everywhere — very aggressive placement |

> In Minecraft 1.20.1, hostile mobs spawn when the block light level is **0**. A threshold of 7 keeps your surroundings well lit with some breathing room.

### Placement Mode Guide

- **Walls & Floor** — prefers side walls, falls back to the front wall, then the floor.
- **Walls Only** — only places wall torches. Useful in structured builds where floor clutter is unwanted.
- **Floor Only** — only places torches on the ground. Good for wide-open flat areas.

## Tips

- Keep torches in your inventory — the mod uses them up just like placing manually.
- Wood torches work identically to vanilla torches for auto-placement. Mix types freely.
- A smaller scan radius (3–4) places torches closer to you, giving more precise coverage in tight caves.
- A higher light threshold (10–12) is useful in the Nether or End where you want aggressive lighting.
- The config is saved per-installation to `config/torch-placer.json` and persists between sessions.

## License

This mod is free to use. You are welcome to include it in modpacks or use it in your own Minecraft game without any restrictions.
