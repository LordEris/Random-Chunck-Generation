package fr.lorderis.randomchunks.data;

import fr.lorderis.randomchunks.RandomChunksPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkDataManager {

    private final RandomChunksPlugin plugin;
    private final File dataFile;
    private final Set<String> transformedChunks = new HashSet<>();

    public ChunkDataManager(RandomChunksPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "transformed_chunks.yml");
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        List<String> keys = config.getStringList("chunks");
        transformedChunks.addAll(keys);
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

    public boolean isTransformed(String worldName, int chunkX, int chunkZ) {
        return transformedChunks.contains(key(worldName, chunkX, chunkZ));
    }

    public void markTransformed(String worldName, int chunkX, int chunkZ) {
        transformedChunks.add(key(worldName, chunkX, chunkZ));
    }

    public void resetWorld(String worldName) {
        transformedChunks.removeIf(k -> k.startsWith(worldName + ":"));
    }

    public int totalTransformed() {
        return transformedChunks.size();
    }

    private String key(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }
}
