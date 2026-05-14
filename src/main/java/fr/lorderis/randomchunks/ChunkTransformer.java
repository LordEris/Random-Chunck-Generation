package fr.lorderis.randomchunks;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public final class ChunkTransformer {

    private ChunkTransformer() {}

    public static void transform(RandomChunksPlugin plugin, World world, Chunk chunk) {
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

    public static boolean isProtectedSpawn(RandomChunksPlugin plugin, World world, int cx, int cz) {
        int radius = plugin.getConfig().getInt("spawn-protection-radius", 2);
        if (radius <= 0) return false;
        int spawnCX = world.getSpawnLocation().getBlockX() >> 4;
        int spawnCZ = world.getSpawnLocation().getBlockZ() >> 4;
        return Math.abs(cx - spawnCX) <= radius && Math.abs(cz - spawnCZ) <= radius;
    }
}
