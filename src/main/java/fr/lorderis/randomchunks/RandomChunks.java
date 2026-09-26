package fr.lorderis.randomchunks;

import fr.lorderis.randomchunks.config.RcgConfig;
import fr.lorderis.randomchunks.gen.BlockPool;
import fr.lorderis.randomchunks.pregen.PregenTask;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Entry point.
 *
 * <p>Every chunk the world generates is rebuilt out of one single block, picked at random from
 * the world seed. Terrain, caves, ores, trees and structures all keep their shape; only what they
 * are made of changes. Air, water, lava and (by default) bedrock are left alone.
 */
public final class RandomChunks implements ModInitializer {
    public static final String MOD_ID = "randomchunks";
    public static final Logger LOGGER = LoggerFactory.getLogger("Random Chunks");

    private static volatile RcgConfig config = new RcgConfig();
    private static volatile BlockPool pool = BlockPool.EMPTY;

    private static final AtomicLong filledChunks = new AtomicLong();

    /** Dimensions already announced this server session, so the line below is logged once each. */
    private static final Set<String> announced = ConcurrentHashMap.newKeySet();

    @Override
    public void onInitialize() {
        config = RcgConfig.loadOrCreate();
        if (config.enabled) {
            LOGGER.info("Random chunks enabled for: {}", String.join(", ", config.dimensions));
        } else {
            LOGGER.info("Random chunks are disabled in the config, world generation is untouched.");
        }
    }

    public static RcgConfig config() {
        return config;
    }

    public static BlockPool pool() {
        return pool;
    }

    public static long filledChunks() {
        return filledChunks.get();
    }

    /**
     * Called when a server starts loading its worlds: block tags are bound by then, which the pool
     * needs, and no chunk has been generated yet.
     */
    public static void onServerStarting() {
        announced.clear();
        filledChunks.set(0);
        // A task left over from a previous single-player session points at a closed world.
        PregenTask.abandon();
        rebuildPool();
    }

    /** Reads the config file again and rebuilds the pool from it. */
    public static BlockPool reload() {
        config = RcgConfig.loadOrCreate();
        return rebuildPool();
    }

    private static BlockPool rebuildPool() {
        BlockPool built = BlockPool.build(config);
        for (String warning : built.warnings()) {
            LOGGER.warn(warning);
        }
        if (built.isEmpty()) {
            LOGGER.error("The block pool is empty: no chunk will be changed. Check blockPool and excludedBlocks in {}.",
                    RcgConfig.FILE_NAME);
        } else {
            LOGGER.info("{} blocks in the pool.", built.size());
        }
        pool = built;
        return built;
    }

    public static void onChunkFilled(Identifier dimension, ChunkPos pos, BlockState block) {
        filledChunks.incrementAndGet();
        // Without this line, a world that generates normally gives no way to tell whether the hook
        // never fired, the dimension is not targeted, or the chunks predate the mod.
        if (announced.add(dimension.toString())) {
            LOGGER.info("Filling new chunks of {} - first one at {} made of {}",
                    dimension, pos, BuiltInRegistries.BLOCK.getKey(block.getBlock()));
        }
    }
}
