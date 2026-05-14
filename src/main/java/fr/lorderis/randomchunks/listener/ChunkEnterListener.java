package fr.lorderis.randomchunks.listener;

import fr.lorderis.randomchunks.ChunkTransformer;
import fr.lorderis.randomchunks.RandomChunksPlugin;
import fr.lorderis.randomchunks.RandomChunksPopulator;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.List;

public class ChunkEnterListener implements Listener {

    private final RandomChunksPlugin plugin;
    private final RandomChunksPopulator populator;

    public ChunkEnterListener(RandomChunksPlugin plugin, RandomChunksPopulator populator) {
        this.plugin = plugin;
        this.populator = populator;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (enabledWorlds.isEmpty() || enabledWorlds.contains(world.getName())) {
            world.getPopulators().add(populator);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(world.getName())) return;
        if (ChunkTransformer.isProtectedSpawn(plugin, world, chunk.getX(), chunk.getZ())) return;
        if (plugin.getDataManager().isTransformed(world.getName(), chunk.getX(), chunk.getZ())) return;

        plugin.getDataManager().markTransformed(world.getName(), chunk.getX(), chunk.getZ());
        ChunkTransformer.transform(plugin, world, chunk);
    }
}
