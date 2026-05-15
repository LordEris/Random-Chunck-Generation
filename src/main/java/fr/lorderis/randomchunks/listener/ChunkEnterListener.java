package fr.lorderis.randomchunks.listener;

import fr.lorderis.randomchunks.ChunkTransformer;
import fr.lorderis.randomchunks.RandomChunksPlugin;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class ChunkEnterListener implements Listener {

    private final RandomChunksPlugin plugin;

    public ChunkEnterListener(RandomChunksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        int fromCX = event.getFrom().getBlockX() >> 4;
        int fromCZ = event.getFrom().getBlockZ() >> 4;
        int toCX   = event.getTo().getBlockX() >> 4;
        int toCZ   = event.getTo().getBlockZ() >> 4;

        if (fromCX == toCX && fromCZ == toCZ) return;

        World world = event.getTo().getWorld();
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(world.getName())) return;

        // 1. Transformer uniquement le chunk dans lequel le joueur entre
        if (!ChunkTransformer.isProtectedSpawn(plugin, world, toCX, toCZ)
                && !plugin.getDataManager().isTransformed(world.getName(), toCX, toCZ)) {
            plugin.getDataManager().markTransformed(world.getName(), toCX, toCZ);
            ChunkTransformer.transform(plugin, world, world.getChunkAt(toCX, toCZ));
        }

        // 2. Pré-calculer (sans transformer) les chunks dans le rayon autour
        int radius = plugin.getConfig().getInt("transform-radius", 3);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                int nx = toCX + dx;
                int nz = toCZ + dz;
                if (ChunkTransformer.isProtectedSpawn(plugin, world, nx, nz)) continue;
                if (plugin.getDataManager().isTransformed(world.getName(), nx, nz)) continue;
                if (ChunkTransformer.hasPrecomputed(world.getName(), nx, nz)) continue;
                if (!world.isChunkLoaded(nx, nz)) continue;
                ChunkTransformer.precompute(plugin, world, nx, nz);
            }
        }
    }
}
