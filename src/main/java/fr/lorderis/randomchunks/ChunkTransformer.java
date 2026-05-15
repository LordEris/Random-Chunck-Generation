package fr.lorderis.randomchunks;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public final class ChunkTransformer {

    private ChunkTransformer() {}

    private static final ConcurrentHashMap<String, PendingTransform> cache = new ConcurrentHashMap<>();

    public static class PendingTransform {
        final Material material;
        final int[] buf;
        final int count;
        PendingTransform(Material material, int[] buf, int count) {
            this.material = material;
            this.buf = buf;
            this.count = count;
        }
    }

    private static String key(String world, int cx, int cz) {
        return world + ":" + cx + ":" + cz;
    }

    public static boolean hasPrecomputed(String world, int cx, int cz) {
        return cache.containsKey(key(world, cx, cz));
    }

    // Appelé depuis le main thread : prend le snapshot et calcule le buffer en async
    public static void precompute(RandomChunksPlugin plugin, World world, int cx, int cz) {
        String k = key(world.getName(), cx, cz);
        if (cache.containsKey(k)) return;

        Chunk chunk = world.getChunkAt(cx, cz);
        ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, false, false);
        Material material = plugin.getBlockPool().random();

        int minY = Math.max(plugin.getConfig().getInt("min-y", world.getMinHeight()), world.getMinHeight());
        int maxY = Math.min(plugin.getConfig().getInt("max-y", world.getMaxHeight() - 1), world.getMaxHeight() - 1);
        boolean preserveBedrock = plugin.getConfig().getBoolean("preserve-bedrock", true);
        BlockPool pool = plugin.getBlockPool();
        String worldName = world.getName();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int[] buf = new int[16 * 16 * (maxY - minY + 1) * 3];
            int count = 0;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        Material type = snapshot.getBlockType(x, y, z);
                        if (type.isAir()) continue;
                        if (type == material) continue;
                        if (preserveBedrock && type == Material.BEDROCK) continue;
                        if (pool.isExcluded(type)) continue;
                        buf[count    ] = x;
                        buf[count + 1] = y;
                        buf[count + 2] = z;
                        count += 3;
                    }
                }
            }
            int[] trimmed = Arrays.copyOf(buf, count);
            int finalCount = count;

            // Stockage sur le main thread pour éviter toute race condition
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!plugin.getDataManager().isTransformed(worldName, cx, cz)) {
                    cache.put(k, new PendingTransform(material, trimmed, finalCount));
                }
            });
        });
    }

    // Appelé depuis le main thread : utilise le cache si dispo, sinon calcule sur place
    public static void transform(RandomChunksPlugin plugin, World world, Chunk chunk) {
        String k = key(world.getName(), chunk.getX(), chunk.getZ());
        PendingTransform pending = cache.remove(k);

        if (pending != null) {
            BlockData targetData = pending.material.createBlockData();
            for (int i = 0; i < pending.count; i += 3) {
                chunk.getBlock(pending.buf[i], pending.buf[i + 1], pending.buf[i + 2]).setBlockData(targetData, false);
            }
        } else {
            transformSync(plugin, world, chunk);
        }
    }

    private static void transformSync(RandomChunksPlugin plugin, World world, Chunk chunk) {
        int minY = Math.max(plugin.getConfig().getInt("min-y", world.getMinHeight()), world.getMinHeight());
        int maxY = Math.min(plugin.getConfig().getInt("max-y", world.getMaxHeight() - 1), world.getMaxHeight() - 1);
        boolean preserveBedrock = plugin.getConfig().getBoolean("preserve-bedrock", true);
        Material material = plugin.getBlockPool().random();

        ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, false, false);
        int[] buf = new int[16 * 16 * (maxY - minY + 1) * 3];
        int count = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Material type = snapshot.getBlockType(x, y, z);
                    if (type.isAir()) continue;
                    if (type == material) continue;
                    if (preserveBedrock && type == Material.BEDROCK) continue;
                    if (plugin.getBlockPool().isExcluded(type)) continue;
                    buf[count    ] = x;
                    buf[count + 1] = y;
                    buf[count + 2] = z;
                    count += 3;
                }
            }
        }

        BlockData targetData = material.createBlockData();
        for (int i = 0; i < count; i += 3) {
            chunk.getBlock(buf[i], buf[i + 1], buf[i + 2]).setBlockData(targetData, false);
        }
    }

    public static void evictWorld(String worldName) {
        cache.keySet().removeIf(k -> k.startsWith(worldName + ":"));
    }

    public static void clearCache() {
        cache.clear();
    }

    public static boolean isProtectedSpawn(RandomChunksPlugin plugin, World world, int cx, int cz) {
        int radius = plugin.getConfig().getInt("spawn-protection-radius", 2);
        if (radius <= 0) return false;
        int spawnCX = world.getSpawnLocation().getBlockX() >> 4;
        int spawnCZ = world.getSpawnLocation().getBlockZ() >> 4;
        return Math.abs(cx - spawnCX) <= radius && Math.abs(cz - spawnCZ) <= radius;
    }
}
