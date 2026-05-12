package fr.lorderis.randomchunks.listener;

import fr.lorderis.randomchunks.RandomChunksPlugin;
import fr.lorderis.randomchunks.data.ChunkDataManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
        if (event.getFrom().getBlockX() >> 4 == event.getTo().getBlockX() >> 4
                && event.getFrom().getBlockZ() >> 4 == event.getTo().getBlockZ() >> 4) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();

        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(world.getName())) {
            return;
        }

        Chunk chunk = event.getTo().getChunk();
        int cx = chunk.getX();
        int cz = chunk.getZ();
        ChunkDataManager dataManager = plugin.getDataManager();

        if (dataManager.isTransformed(world.getName(), cx, cz)) {
            return;
        }

        dataManager.markTransformed(world.getName(), cx, cz);

        Material chosen = plugin.getBlockPool().random();
        transformChunk(world, chunk, chosen);

        if (plugin.getConfig().getBoolean("notify-player", true)) {
            String msg = plugin.getConfig()
                    .getString("notify-message", "&7[&bRandomChunks&7] &fChunk transformé en &e{block}&f !")
                    .replace("{block}", chosen.name())
                    .replace("&", "§");
            player.sendMessage(msg);
        }
    }

    private void transformChunk(World world, Chunk chunk, Material material) {
        int minY = plugin.getConfig().getInt("min-y", world.getMinHeight());
        int maxY = plugin.getConfig().getInt("max-y", world.getMaxHeight() - 1);
        boolean preserveBedrock = plugin.getConfig().getBoolean("preserve-bedrock", true);

        minY = Math.max(minY, world.getMinHeight());
        maxY = Math.min(maxY, world.getMaxHeight() - 1);

        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = world.getBlockAt(baseX + x, y, baseZ + z);

                    Material type = block.getType();

                    if (type == Material.WATER || type == Material.LAVA) {
                        continue;
                    }

                    if (preserveBedrock && type == Material.BEDROCK) {
                        continue;
                    }

                    block.setType(material, false);
                }
            }
        }
    }
}
