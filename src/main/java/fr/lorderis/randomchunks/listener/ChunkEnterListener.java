package fr.lorderis.randomchunks.listener;

import fr.lorderis.randomchunks.RandomChunksPlugin;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.List;

public class ChunkEnterListener implements Listener {

    private final RandomChunksPlugin plugin;

    public ChunkEnterListener(RandomChunksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(world.getName())) return;

        if (plugin.getDataManager().isTransformed(world.getName(), chunk.getX(), chunk.getZ())) return;

        int radius = plugin.getConfig().getInt("spawn-protection-radius", 10);
        if (radius > 0) {
            int spawnCX = world.getSpawnLocation().getBlockX() >> 4;
            int spawnCZ = world.getSpawnLocation().getBlockZ() >> 4;
            if (Math.abs(chunk.getX() - spawnCX) <= radius && Math.abs(chunk.getZ() - spawnCZ) <= radius) return;
        }

        plugin.getDataManager().markTransformed(world.getName(), chunk.getX(), chunk.getZ());
        transform(world, chunk);
    }

    private void transform(World world, Chunk chunk) {
        int minY = Math.max(plugin.getConfig().getInt("min-y", world.getMinHeight()), world.getMinHeight());
        int maxY = Math.min(plugin.getConfig().getInt("max-y", world.getMaxHeight() - 1), world.getMaxHeight() - 1);
        boolean preserveBedrock = plugin.getConfig().getBoolean("preserve-bedrock", true);
        Material material = plugin.getBlockPool().random();

        ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, false, false);
        int capacity = 16 * 16 * (maxY - minY + 1);
        int[] buf = new int[capacity * 3];
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
}
