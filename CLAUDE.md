# Torch Placer Mod — Claude Context

## Project Overview
A Fabric mod for Minecraft **1.20.1** that automatically places torches in dark areas around the player. The player can toggle the feature with a keybinding and configure it through an in-game settings screen. Supports 11 wood-variant torch types (oak through warped) in addition to the vanilla torch.

## Key Facts
- **Mod ID**: `torch-placer`
- **Mappings**: Official Mojang mappings (NOT Yarn). All Minecraft class names follow Mojang conventions.
- **Fabric API version**: 0.92.7+1.20.1
- **Java**: 17 (source/target), requires Java 21+ JVM to run Gradle due to Fabric Loom 1.16-SNAPSHOT
- **GitHub**: https://github.com/KunalP99/torch-placer-1.20.1
- **License**: Free to use — anyone can include it in modpacks or their own game. No CC0.

## Building
Gradle requires a Java 21+ JVM (Loom 1.16 constraint). Use:
```
JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-21.0.4.7-hotspot" ./gradlew build
```
The compiled mod still targets Java 17 bytecode.

## Mojang Mappings — Common Gotchas
This is the most common source of bugs. Always use Mojang names, never Yarn names.

| Concept | Mojang (correct) | Yarn (wrong — do NOT use) |
|---|---|---|
| Block classes | `net.minecraft.world.level.block.*` | `net.minecraft.block.*` |
| Item classes | `net.minecraft.world.item.*` | `net.minecraft.item.*` |
| BlockPos | `net.minecraft.core.BlockPos` | `net.minecraft.util.math.BlockPos` |
| Direction | `net.minecraft.core.Direction` | `net.minecraft.util.math.Direction` |
| ServerPlayer | `net.minecraft.server.level.ServerPlayer` | `ServerPlayerEntity` |
| ServerLevel | `net.minecraft.server.level.ServerLevel` | `ServerWorld` |
| LightLayer | `net.minecraft.world.level.LightLayer` | `LightType` |
| ResourceLocation | `net.minecraft.resources.ResourceLocation` | `Identifier` |
| FriendlyByteBuf | `net.minecraft.network.FriendlyByteBuf` | `PacketByteBuf` |
| KeyMapping | `net.minecraft.client.KeyMapping` | `KeyBinding` |
| InputConstants | `com.mojang.blaze3d.platform.InputConstants` | `InputUtil` |
| Screen | `net.minecraft.client.gui.screens.Screen` | `net.minecraft.client.gui.screen.Screen` |
| Button | `net.minecraft.client.gui.components.Button` | `ButtonWidget` |
| AbstractSliderButton | `net.minecraft.client.gui.components.AbstractSliderButton` | `SliderWidget` |
| Tooltip | `net.minecraft.client.gui.components.Tooltip` | — |
| GuiGraphics | `net.minecraft.client.gui.GuiGraphics` | `DrawContext` |
| Component | `net.minecraft.network.chat.Component` | `Text` |
| BuiltInRegistries | `net.minecraft.core.registries.BuiltInRegistries` | `net.minecraft.core.BuiltInRegistries` (moved in Loom 1.16) |
| server.getTickCount() | `getTickCount()` | `getTicks()` |
| server.getPlayerList() | `getPlayerList()` | `getPlayerManager()` |
| player.blockPosition() | `blockPosition()` | `getBlockPos()` |
| player.getUUID() | `getUUID()` | `getUuid()` |
| player.displayClientMessage() | `displayClientMessage(component, overlay)` | `sendMessage(text, overlay)` |
| player.getDirection() | `getDirection()` | same |
| pos.offset(x,y,z) | `offset(x,y,z)` | `add(x,y,z)` |
| pos.relative(dir) | `relative(dir)` | `offset(dir)` |
| pos.below() | `below()` | `down()` |
| Direction.Plane.HORIZONTAL | `Direction.Plane.HORIZONTAL` | `Direction.Type.HORIZONTAL` |
| world.getBrightness() | `getBrightness(LightLayer.BLOCK, pos)` | `getLightLevel(LightType.BLOCK, pos)` |
| world.setBlock() | `setBlock(pos, state, 3)` | `setBlockState(pos, state)` |
| blockState.defaultBlockState() | `defaultBlockState()` | `getDefaultState()` |
| blockState.isFaceSturdy() | `isFaceSturdy(world, pos, dir)` | `isSideSolidFullSquare(...)` |
| inventory.getItem(i) | `getItem(i)` | `getStack(i)` |
| inventory.getContainerSize() | `getContainerSize()` | `size()` |
| itemStack.is(item) | `is(item)` | `isOf(item)` |
| itemStack.shrink(1) | `shrink(1)` | `decrement(1)` |
| addRenderableWidget() | `addRenderableWidget(widget)` | `addDrawableChild(widget)` |
| KeyMapping.consumeClick() | `consumeClick()` | `wasPressed()` |
| client.screen | `client.screen` | `client.currentScreen` |
| client.getConnection() | `getConnection()` | `getNetworkHandler()` |
| Button.builder().bounds() | `.bounds(x,y,w,h)` | `.dimensions(x,y,w,h)` |
| TorchBlock constructor | `new TorchBlock(Properties, ParticleOptions)` | `new TorchBlock(ParticleOptions, Properties)` |
| StandingAndWallBlockItem | `new StandingAndWallBlockItem(floor, wall, props, Direction)` | (no Direction in older Loom) |

## Source Structure

```
src/
├── main/java/torchplacer/
│   ├── TorchPlacer.java          — ModInitializer; registers ModBlocks, ModItems, network, tick event
│   ├── TorchPlacerConfig.java    — Config POJO; GSON load/save to config/torch-placer.json
│   ├── TorchPlacerLogic.java     — Server-side tick logic; scans blocks, places torches
│   ├── TorchPlacerNetwork.java   — C2S packet (torch-placer:config_sync); per-player config store
│   ├── PlacementMode.java        — Enum: BOTH, WALLS_ONLY, FLOOR_ONLY
│   ├── WoodTorchVariant.java     — Enum: 11 wood variants (OAK…WARPED) each with an id string
│   ├── ModBlocks.java            — Registers 22 blocks: EnumMap FLOOR + WALL (TorchBlock/WallTorchBlock)
│   ├── ModItems.java             — Registers 11 StandingAndWallBlockItem; adds to creative tab
│   └── mixin/ExampleMixin.java   — Unused template mixin (can be removed)
└── client/java/torchplacer/client/
    ├── TorchPlacerClient.java        — ClientModInitializer; keybindings, tick handler, network sync
    ├── KeyBindings.java              — Two KeyMappings: toggle (G) and config screen (unbound)
    ├── TorchPlacerConfigScreen.java  — In-game settings Screen
    └── mixin/ExampleClientMixin.java — Unused template mixin (can be removed)
```

## How It Works

### Auto-placement (server-side)
Every 40 ticks (~2 seconds), `TorchPlacerLogic.tick()` iterates online players. For each player with `config.enabled = true`:
1. Scans inventory for the first usable torch via `findTorchEntry()`:
   - Checks vanilla `Items.TORCH` first (maps to `Blocks.TORCH` / `Blocks.WALL_TORCH`).
   - Then checks each `WoodTorchVariant` against `ModItems.ITEMS` (maps to `ModBlocks.FLOOR` / `ModBlocks.WALL`).
   - Returns a `TorchEntry(slot, floorBlock, wallBlock)` record, or `null` if none found.
2. Scans all blocks within `scanRadius` (±radius X/Z, -2 to +3 Y) of the player.
3. For each air block where `getBrightness(LightLayer.BLOCK, pos) <= lightThreshold`:
   - **Wall candidate**: checks horizontal neighbors for `isFaceSturdy`. Places `entry.wall()` with `WallTorchBlock.FACING` set away from the wall.
   - **Floor candidate**: checks `isFaceSturdy(world, below, Direction.UP)`. Places `entry.floor()`.
4. **Placement priority** (sorted by):
   1. Wall type — side walls (left/right of `player.getDirection()`) = priority 0, front/back walls = priority 1, floor = priority 2
   2. Light level (darkest first)
   3. Distance (closest first)
5. Places one torch per trigger, removes it from the player's inventory with `shrink(1)`.

### Wood-variant torches
11 custom torch types, one per Minecraft 1.20.1 wood type. Each variant is:
- A `TorchBlock` (floor) + `WallTorchBlock` (wall) registered under `torch-placer:<wood>_torch` / `torch-placer:<wood>_wall_torch`.
- A `StandingAndWallBlockItem` (with `Direction.DOWN`) registered under `torch-placer:<wood>_torch`.
- Crafted with the matching wood plank (on top) + vanilla torch (below) in any crafting grid — yields 1.
- Textured via `assets/torch-placer/textures/block/<wood>_torch.png` — **custom PNGs must be provided** (the game shows a magenta checkerboard until they are added). Both the floor block, wall block, and item all reference the same texture file.

Variants: `oak`, `spruce`, `birch`, `jungle`, `acacia`, `dark_oak`, `mangrove`, `cherry`, `bamboo`, `crimson`, `warped`.

### Config & networking
- Config is stored client-side in `config/torch-placer.json` (GSON).
- On world join and on any change, client sends `torch-placer:config_sync` C2S packet.
- Server stores received configs in `TorchPlacerNetwork.PLAYER_CONFIGS` (`Map<UUID, TorchPlacerConfig>`).

### Keybindings
- **G** (default) — toggle on/off; shows "Auto Torch: ON/OFF" in action bar via `displayClientMessage(..., true)`.
- **Unbound** — open config screen.

### Config screen (`TorchPlacerConfigScreen`)
- `ThresholdSlider` — light level 0–14; has a dynamic `Tooltip` (via `Tooltip.create()`) that updates on drag with a plain-English description of the current value.
- Placement mode cycle `Button` — cycles BOTH → WALLS_ONLY → FLOOR_ONLY.
- `RadiusSlider` — scan radius 3–10 blocks.
- Save writes to file + sends C2S packet. Cancel discards changes.

## Config Fields (`TorchPlacerConfig`)
| Field | Type | Default | Range |
|---|---|---|---|
| `enabled` | boolean | false | — |
| `lightThreshold` | int | 7 | 0–14 |
| `placementMode` | PlacementMode | BOTH | — |
| `scanRadius` | int | 5 | 3–10 |

## Resources
- `src/main/resources/fabric.mod.json` — mod metadata; entrypoints for main + client
- `src/main/resources/assets/torch-placer/lang/en_us.json` — translation keys for keybindings, screen labels, and 11 wood torch item names
- `src/main/resources/assets/torch-placer/models/block/template_wood_torch.json` — shared floor torch model (geometry from vanilla `template_torch.json`)
- `src/main/resources/assets/torch-placer/models/block/template_wood_torch_wall.json` — shared wall torch model (geometry from vanilla `template_torch_wall.json`)
- `src/main/resources/assets/torch-placer/blockstates/` — 22 blockstate JSONs (11 floor + 11 wall)
- `src/main/resources/assets/torch-placer/models/block/` — 22 per-variant block models + 2 templates
- `src/main/resources/assets/torch-placer/models/item/` — 11 per-variant item models
- `src/main/resources/assets/torch-placer/textures/block/` — **11 custom PNGs go here** (`<wood>_torch.png`)
- `src/main/resources/data/torch-placer/recipes/` — 11 shaped crafting recipes
- `src/main/resources/data/torch-placer/loot_tables/blocks/` — 22 loot tables (blocks drop their item)
- `src/client/resources/torch-placer.client.mixins.json` — client mixin config
- `src/main/resources/torch-placer.mixins.json` — server mixin config
