# Random Chunks — Modrinth page

## Summary

> Every chunk is made of one random block. Terrain, caves, ores, trees and structures keep their
> shape, only the material changes. Seed-based, configurable, with a built-in pregenerator. Fabric
> 26.3, no Fabric API needed.

## Description

**Random Chunks** turns every chunk of your world into a single random block. A mountain of
diamond blocks next to a forest of pink wool next to a village made entirely of ice: the world keeps
its shape, but every chunk is rebuilt out of one block type picked at random.

It is made for challenge runs, videos and chaotic survival: can you beat the game when the chunk you
spawn next to is solid obsidian and the one after it is slime?

### Features

- **One block per chunk.** Terrain, caves, ores, trees, plants, geodes and structures (villages,
  temples, fortresses, bastions, end cities, trial chambers, abandoned camps…) are all converted.
  Air, water, lava and bedrock are left as they are, so caves, ravines and oceans keep their shape.
- **All dimensions.** The Overworld, the Nether, the End, and modded dimensions too.
- **Seed-based.** The block of each chunk comes from the world seed and the chunk position: the same
  seed always gives the same world, so seeds can be shared.
- **Seamless.** Chunks are converted while they generate, before anyone sees them. There is no
  flicker when walking into a chunk, and no lag spike on the server thread.
- **Clean conversion.** Trees, ore veins and structure pieces spilling over from neighbouring chunks
  are converted too, containers are removed without dropping their loot, villages lose their beds
  and job sites properly, and light and heightmaps are computed on the final chunk.
- **Sensible block pool.** Around 590 blocks can be picked in 26.3. Blocks that make no sense as a
  whole chunk are left out: plants, fences, walls, panes, doors, rails, torches, carpets, beds,
  blocks that fall (sand, gravel, concrete powder), blocks with an inventory, TNT, bedrock and
  barriers. Leaves are placed persistent so they never decay.
- **Spawn protection.** The chunks around the world spawn are left untouched by default, so you do
  not start inside a solid block.
- **Built-in pregenerator** to generate an area ahead of time, asynchronously, without freezing the
  server.
- **Server-side only.** Install it on the server; players join with a vanilla client. Works in
  single-player too.

### Commands

All commands need operator level 2 (or the server console). `/rcg` is a shortcut for
`/randomchunks`.

| Command | What it does |
|---|---|
| `/randomchunks reload` | Reloads the config and rebuilds the block pool |
| `/randomchunks info` | Shows the number of blocks in the pool, chunks filled since startup and the main settings |
| `/randomchunks pregen start <dimension> <radius> [x z]` | Generates a square of `(2 × radius + 1)²` chunks around `x z`, or around the world spawn (0 0 in the other dimensions) |
| `/randomchunks pregen stop` | Cancels the running pregeneration |
| `/randomchunks pregen status` | Shows its progress |

The pregenerator works ring by ring from the centre, keeps only a handful of chunks loaded at a
time, logs its progress with an ETA every 100 chunks, and keeps running when nobody is online.

### Configuration

`config/random-chunks.json`, created on first launch. Settings added by a mod update are appended to
an existing file automatically.

| Setting | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Master switch |
| `dimensions` | `["*"]` | Dimensions whose new chunks are converted. `"*"` means all of them |
| `blockPool` | `[]` | When not empty, the only blocks that can be picked (ids or `#tags`), e.g. `["#minecraft:wool", "minecraft:glass"]` |
| `excludedBlocks` | plants, thin blocks, TNT… | Blocks that are never picked (ids or `#tags`) |
| `preserveBedrock` | `true` | Keep the bedrock floor and the Nether ceiling |
| `minY` / `maxY` | `-64` / `320` | Height range that is converted, clamped to each dimension |
| `spawnProtectionRadius` | `2` | Radius in chunks left vanilla around the world spawn. `0` converts everything |
| `pregenChunksPerTick` | `10` | Chunks requested per tick by the pregenerator |

Air, fluids, blocks with a block entity, falling blocks and blocks without an item form can never be
picked, even when listed in `blockPool`.

### Compatibility

- Minecraft **26.3**, Fabric Loader **0.19.5** or newer, Java 25.
- **Fabric API is not required.**
- Checked against every block and structure added since 1.21: pale oak, resin, eyeblossoms, dry
  grass and bushes, dried ghasts, copper lanterns, chains, bars and shelves, golden dandelions,
  sulfur and cinnabar, poplar, wool and concrete slabs and stairs, straw beds, shelf mushrooms,
  and the new abandoned camps.
- Only **new** chunks are converted: create a new world, or explore past what is already generated.

### Good to know

- Mobs placed by structures (villagers, witches…) end up inside their chunk's block and suffocate.
- Water and lava stay where generation put them and can flow into a neighbouring chunk whose block
  lets fluids through.
- Old worlds upgraded from before 1.18 and the debug world are never touched.

### License

[CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/): free to use, modify and share with
credit, but not to sell. **Videos and streams are explicitly allowed, monetised ones included.**
Source code on [GitHub](https://github.com/LordEris/Random-Chunck-Generation).
