package fr.lorderis.randomchunks.listener;

import fr.lorderis.randomchunks.ChunkTransformer;
import fr.lorderis.randomchunks.RandomChunksPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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

        handleChunkEnter(event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;

        int fromCX = event.getFrom().getBlockX() >> 4;
        int fromCZ = event.getFrom().getBlockZ() >> 4;
        int toCX   = event.getTo().getBlockX() >> 4;
        int toCZ   = event.getTo().getBlockZ() >> 4;

        if (fromCX == toCX && fromCZ == toCZ) return;

        handleChunkEnter(event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        handleChunkEnter(event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        handleChunkEnter(event.getRespawnLocation());
    }

    private void handleChunkEnter(Location loc) {
        if (loc == null || loc.getWorld() == null) return;

        World world = loc.getWorld();
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(world.getName())) return;

        int cx = loc.getBlockX() >> 4;
        int cz = loc.getBlockZ() >> 4;

        // 1. Transformer uniquement le chunk dans lequel le joueur entre
        if (!ChunkTransformer.isProtectedSpawn(plugin, world, cx, cz)
                && !plugin.getDataManager().isTransformed(world.getName(), cx, cz)) {
            plugin.getDataManager().markTransformed(world.getName(), cx, cz);
            ChunkTransformer.transform(plugin, world, world.getChunkAt(cx, cz));
        }

        // 2. Pré-calculer (sans transformer) les chunks dans le rayon autour
        int radius = plugin.getConfig().getInt("transform-radius", 3);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                int nx = cx + dx;
                int nz = cz + dz;
                if (ChunkTransformer.isProtectedSpawn(plugin, world, nx, nz)) continue;
                if (plugin.getDataManager().isTransformed(world.getName(), nx, nz)) continue;
                if (ChunkTransformer.hasPrecomputed(world.getName(), nx, nz)) continue;
                if (!world.isChunkLoaded(nx, nz)) continue;
                ChunkTransformer.precompute(plugin, world, nx, nz);
            }
        }
    }
}
