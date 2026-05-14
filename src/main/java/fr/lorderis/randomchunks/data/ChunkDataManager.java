package fr.lorderis.randomchunks.data;

import fr.lorderis.randomchunks.RandomChunksPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkDataManager {

    private final RandomChunksPlugin plugin;
    private final File dataFile;
    private final Set<String> transformedChunks = ConcurrentHashMap.newKeySet();

    public ChunkDataManager(RandomChunksPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "transformed_chunks.yml");
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        transformedChunks.addAll(config.getStringList("chunks"));
        plugin.getLogger().info(transformedChunks.size() + " chunks transformés chargés.");
    }

    public void save() {
        if (!plugin.getConfig().getBoolean("persist-data", true)) return;
        YamlConfiguration config = new YamlConfiguration();
        config.set("chunks", List.copyOf(transformedChunks));
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder transformed_chunks.yml : " + e.getMessage());
        }
    }

    public boolean isTransformed(String world, int cx, int cz) {
        return transformedChunks.contains(key(world, cx, cz));
    }

    public void markTransformed(String world, int cx, int cz) {
        transformedChunks.add(key(world, cx, cz));
    }

    public void resetWorld(String worldName) {
        transformedChunks.removeIf(k -> k.startsWith(worldName + ":"));
    }

    public int totalTransformed() {
        return transformedChunks.size();
    }

    private static String key(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }
}
