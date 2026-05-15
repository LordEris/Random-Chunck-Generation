package fr.lorderis.randomchunks.pregen;

import fr.lorderis.randomchunks.ChunkTransformer;
import fr.lorderis.randomchunks.RandomChunksPlugin;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class PregenerationTask extends BukkitRunnable {

    private final RandomChunksPlugin plugin;
    private final World world;
    private final int[][] chunks;
    private int index = 0;
    private final int chunksPerTick;
    private long startTime;

    public PregenerationTask(RandomChunksPlugin plugin, World world, int centerCX, int centerCZ, int radius) {
        this.plugin = plugin;
        this.world = world;
        this.chunksPerTick = plugin.getConfig().getInt("pregen-chunks-per-tick", 2);

        List<int[]> list = new ArrayList<>();
        for (int r = 0; r <= radius; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) == r || Math.abs(z) == r) {
                        list.add(new int[]{centerCX + x, centerCZ + z});
                    }
                }
            }
        }
        this.chunks = list.toArray(new int[0][]);
    }

    @Override
    public void run() {
        if (index == 0) {
            startTime = System.currentTimeMillis();
            plugin.getLogger().info("[Pregen] Démarrage sur " + world.getName()
                    + " — " + chunks.length + " chunks à traiter (" + chunksPerTick + " chunks/tick).");
        }

        int processed = 0;
        while (index < chunks.length && processed < chunksPerTick) {
            int cx = chunks[index][0];
            int cz = chunks[index][1];
            index++;
            processed++;

            if (!plugin.getDataManager().isTransformed(world.getName(), cx, cz)) {
                if (ChunkTransformer.isProtectedSpawn(plugin, world, cx, cz)) continue;
                world.loadChunk(cx, cz, true);
                Chunk chunk = world.getChunkAt(cx, cz);
                plugin.getDataManager().markTransformed(world.getName(), cx, cz);
                ChunkTransformer.transform(plugin, world, chunk);
                world.unloadChunkRequest(cx, cz);
            }
        }

        if (index % 100 == 0 || index >= chunks.length) {
            int pct = index * 100 / chunks.length;
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            String eta;
            if (index > 0 && index < chunks.length) {
                long etaSec = elapsed * (chunks.length - index) / index;
                eta = " — ETA " + formatTime(etaSec);
            } else {
                eta = "";
            }
            plugin.getLogger().info("[Pregen] " + world.getName()
                    + " : " + index + "/" + chunks.length + " (" + pct + "%) — " + formatTime(elapsed) + " écoulées" + eta);
        }

        if (index >= chunks.length) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            plugin.getLogger().info("[Pregen] " + world.getName()
                    + " terminé en " + formatTime(elapsed) + " — " + chunks.length + " chunks générés.");
            plugin.getDataManager().save();
            plugin.setPregenerationTask(null);
            cancel();
        }
    }

    public int getProgress() { return index; }
    public int getTotal() { return chunks.length; }
    public String getWorldName() { return world.getName(); }

    private static String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m" + (seconds % 60) + "s";
        return (seconds / 3600) + "h" + ((seconds % 3600) / 60) + "m";
    }
}
