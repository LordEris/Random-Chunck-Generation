package fr.lorderis.randomchunks.gen;

import fr.lorderis.randomchunks.RandomChunks;
import fr.lorderis.randomchunks.config.RcgConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Rewrites every solid block of a freshly generated chunk into the chunk's random block.
 *
 * <p>It runs at the start of the {@code LIGHT} generation step. That step only starts once every
 * neighbouring chunk has finished its own {@code FEATURES} step, and decoration is the last thing
 * allowed to write into a neighbour: past that point the chunk's blocks are final, including the
 * trees, ore veins and structure pieces its neighbours spilled into it. Filling it any earlier
 * would let those spills land untouched on top of the random block.
 */
public final class ChunkFiller {
    /**
     * Both the final heightmaps and the world-gen ones. A plant or a leaf turned into a full block,
     * or a stone turned into cobweb, moves the surface these describe.
     */
    private static final Set<Heightmap.Types> HEIGHTMAPS = EnumSet.allOf(Heightmap.Types.class);

    private ChunkFiller() {
    }

    /** Called at the head of the {@code LIGHT} step of every chunk. */
    public static void onLightStep(ServerLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        // An ImposterProtoChunk wraps a chunk that is already fully generated and loaded: the
        // loading pipeline runs this step on it too, and it must never be touched again.
        if (!(chunk instanceof ProtoChunk) || chunk instanceof ImposterProtoChunk) {
            return;
        }
        // Below-zero retrogen replays generation over a chunk from a pre-1.18 world. Its blocks are
        // the old terrain and whatever a player built on it, not something to overwrite.
        if (chunk.isUpgrading()) {
            return;
        }
        // The debug world lays out every block state on a grid; filling it would erase the point.
        if (generator instanceof DebugLevelSource) {
            return;
        }

        RcgConfig config = RandomChunks.config();
        if (!config.enabled) {
            return;
        }
        Identifier dimension = level.dimension().identifier();
        if (!config.appliesToDimension(dimension)) {
            return;
        }
        BlockPool pool = RandomChunks.pool();
        if (pool.isEmpty()) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        if (isProtectedSpawn(level, pos, config)) {
            return;
        }

        BlockState target = pool.pick(level.getSeed(), dimension, pos.x(), pos.z());
        // A chunk saved half-way through generation replays this step when it is loaded again.
        // The pick is deterministic, so that replay finds nothing left to change.
        if (fill(level, chunk, target, config) > 0) {
            RandomChunks.onChunkFilled(dimension, pos, target);
        }
    }

    /**
     * Whether a chunk lies in the untouched square around the world spawn.
     *
     * <p>The spawn is read from the level data rather than the server's cached copy: new chunks
     * are first generated while the initial spawn is being chosen, and by then only the level data
     * has been updated.
     */
    public static boolean isProtectedSpawn(ServerLevel level, ChunkPos pos, RcgConfig config) {
        int radius = config.spawnProtectionRadius;
        if (radius <= 0) {
            return false;
        }
        LevelData.RespawnData spawn = level.getServer().getWorldData().overworldData().getRespawnData();
        if (spawn == null || !spawn.dimension().equals(level.dimension())) {
            return false;
        }
        ChunkPos spawnChunk = ChunkPos.containing(spawn.pos());
        return Math.abs(pos.x() - spawnChunk.x()) <= radius && Math.abs(pos.z() - spawnChunk.z()) <= radius;
    }

    /** Returns how many blocks were rewritten. */
    static int fill(ServerLevel level, ChunkAccess chunk, BlockState target, RcgConfig config) {
        int minY = Math.max(config.minY, chunk.getMinY());
        int maxY = Math.min(config.maxY, chunk.getMaxY());
        if (minY > maxY) {
            return 0;
        }

        LevelChunkSection[] sections = chunk.getSections();
        int originX = chunk.getPos().getMinBlockX();
        int originZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int changed = 0;

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir()) {
                continue;
            }
            int sectionBottomY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
            int fromY = Math.max(minY, sectionBottomY);
            int toY = Math.min(maxY, sectionBottomY + 15);

            for (int worldY = fromY; worldY <= toY; worldY++) {
                int localY = worldY - sectionBottomY;
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        if (!replaces(state, target, config)) {
                            continue;
                        }
                        boolean poi = PoiTypes.hasPoi(state);
                        if (poi || state.hasBlockEntity()) {
                            cursor.set(originX + localX, worldY, originZ + localZ);
                            // Removed rather than left to be dropped: a village chest turned into
                            // stone must not scatter its loot on the ground.
                            if (state.hasBlockEntity()) {
                                chunk.removeBlockEntity(cursor);
                            }
                            // A village bed or workstation registered itself as a point of interest
                            // when the structure placed it; villagers would keep walking to it.
                            if (poi) {
                                level.updatePOIOnBlockStateChange(cursor.immutable(), state, target);
                            }
                        }
                        section.setBlockState(localX, localY, localZ, target, false);
                        changed++;
                    }
                }
            }
        }

        if (changed > 0) {
            resetAndPrimeHeightmaps(chunk);
            // The sky light sources were computed by INITIALIZE_LIGHT from the blocks as they were.
            chunk.initializeLightSources();
            // Only reachable when a chunk saved with its light already computed is filled on load
            // (a config change, or a world created before the mod): its stored light is now wrong.
            if (chunk.isLightCorrect()) {
                chunk.setLightCorrect(false);
            }
        }
        return changed;
    }

    /**
     * Air stays air, so caves and the sky keep their shape; water and lava stay too, as they did
     * in the plugin. Everything else becomes the chunk's block.
     */
    private static boolean replaces(BlockState state, BlockState target, RcgConfig config) {
        if (state == target || state.isAir()) {
            return false;
        }
        if (state.getBlock() instanceof LiquidBlock) {
            return false;
        }
        return !(config.preserveBedrock && state.is(Blocks.BEDROCK));
    }

    /**
     * Priming alone only raises heights: a column whose top block turned from a solid block into
     * something that no longer counts would keep its old height. Clear first, then prime.
     */
    private static void resetAndPrimeHeightmaps(ChunkAccess chunk) {
        for (Heightmap.Types type : HEIGHTMAPS) {
            // An all-zero backing array reads back as the bottom of the world for every column.
            Arrays.fill(chunk.getOrCreateHeightmapUnprimed(type).getRawData(), 0L);
        }
        Heightmap.primeHeightmaps(chunk, HEIGHTMAPS);
    }
}
