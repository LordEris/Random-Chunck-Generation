package fr.lorderis.randomchunks.gen;

import fr.lorderis.randomchunks.config.RcgConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The blocks a chunk can be made of, and the deterministic pick of one of them per chunk.
 *
 * <p>Built once the server has loaded its data, because the exclusions can name block tags and
 * tags are only bound at that point. Immutable afterwards, so worker threads can read it freely.
 */
public final class BlockPool {
    public static final BlockPool EMPTY = new BlockPool(new BlockState[0], List.of());

    private final BlockState[] states;
    private final List<String> warnings;

    private BlockPool(BlockState[] states, List<String> warnings) {
        this.states = states;
        this.warnings = warnings;
    }

    public static BlockPool build(RcgConfig config) {
        List<String> warnings = new ArrayList<>();
        Set<Block> excluded = resolve(config.excludedBlocks, "excludedBlocks", warnings);

        List<Block> candidates = new ArrayList<>();
        if (config.blockPool.isEmpty()) {
            // Registry order is stable for a given game version, which keeps the pick below
            // reproducible for a given seed and config.
            for (Block block : BuiltInRegistries.BLOCK) {
                if (refusal(block) == null && !excluded.contains(block)) {
                    candidates.add(block);
                }
            }
        } else {
            for (Block block : resolve(config.blockPool, "blockPool", warnings)) {
                String refusal = refusal(block);
                if (refusal == null && excluded.contains(block)) {
                    refusal = "listed in excludedBlocks";
                }
                if (refusal != null) {
                    warnings.add("blockPool: " + BuiltInRegistries.BLOCK.getKey(block) + " refused (" + refusal + ")");
                } else {
                    candidates.add(block);
                }
            }
        }

        BlockState[] states = new BlockState[candidates.size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = placedState(candidates.get(i));
        }
        return new BlockPool(states, Collections.unmodifiableList(warnings));
    }

    /**
     * Why a block can never fill a chunk, or {@code null} if it can. These are not a matter of
     * taste, so they apply even to blocks listed in {@code blockPool}:
     * <ul>
     *     <li>air and fluids mean nothing as a chunk filler;</li>
     *     <li>a block entity per block would mean tens of thousands of them per chunk;</li>
     *     <li>falling blocks collapse the moment anything next to them updates;</li>
     *     <li>blocks with no item form are technical or wall/potted/attached variants that only
     *     make sense next to their support.</li>
     * </ul>
     */
    public static String refusal(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.isAir()) {
            return "air";
        }
        if (!state.getFluidState().isEmpty()) {
            return "holds a fluid";
        }
        if (state.hasBlockEntity()) {
            return "has a block entity";
        }
        if (block instanceof Fallable) {
            return "falls";
        }
        if (block.asItem() == Items.AIR) {
            return "has no item form";
        }
        return null;
    }

    /**
     * The state a chunk is filled with. Leaves are made persistent: generated without a log next
     * to them, default leaves would decay and a whole chunk would slowly vanish.
     */
    private static BlockState placedState(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(LeavesBlock.PERSISTENT)) {
            state = state.setValue(LeavesBlock.PERSISTENT, true);
        }
        return state;
    }

    private static Set<Block> resolve(List<String> entries, String key, List<String> warnings) {
        Set<Block> blocks = new LinkedHashSet<>();
        for (String entry : entries) {
            String normalized = RcgConfig.normalizeId(entry);
            if (normalized == null || normalized.equals("*")) {
                continue;
            }
            if (normalized.startsWith("#")) {
                Identifier id = Identifier.tryParse(normalized.substring(1));
                if (id == null) {
                    warnings.add(key + ": invalid tag " + entry);
                    continue;
                }
                boolean any = false;
                for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(TagKey.create(Registries.BLOCK, id))) {
                    blocks.add(holder.value());
                    any = true;
                }
                if (!any) {
                    warnings.add(key + ": unknown or empty tag " + normalized);
                }
            } else {
                Identifier id = Identifier.tryParse(normalized);
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                    warnings.add(key + ": unknown block " + entry);
                    continue;
                }
                blocks.add(BuiltInRegistries.BLOCK.getValue(id));
            }
        }
        return blocks;
    }

    /**
     * The block of one chunk. Derived from the world seed, the dimension and the chunk position
     * only, so the same seed and config always give the same world, and a chunk whose generation
     * is resumed after a restart gets the same block it was about to receive.
     */
    public BlockState pick(long seed, Identifier dimension, int chunkX, int chunkZ) {
        long hash = mix(seed ^ dimension.toString().hashCode());
        hash = mix(hash ^ (((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL)));
        return this.states[(int) Math.floorMod(hash, (long) this.states.length)];
    }

    /** MurmurHash3 finalizer: spreads neighbouring chunk coordinates over the whole pool. */
    private static long mix(long z) {
        z = (z ^ (z >>> 33)) * 0xFF51AFD7ED558CCDL;
        z = (z ^ (z >>> 33)) * 0xC4CEB9FE1A85EC53L;
        return z ^ (z >>> 33);
    }

    public boolean isEmpty() {
        return this.states.length == 0;
    }

    public int size() {
        return this.states.length;
    }

    public List<String> warnings() {
        return this.warnings;
    }
}
