package fr.lorderis.randomchunks;

import fr.lorderis.randomchunks.command.RandomChunksCommand;
import fr.lorderis.randomchunks.data.ChunkDataManager;
import fr.lorderis.randomchunks.listener.ChunkEnterListener;
import fr.lorderis.randomchunks.pregen.PregenerationTask;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class RandomChunksPlugin extends JavaPlugin {

    private ChunkDataManager dataManager;
    private BlockPool blockPool;
    private PregenerationTask pregenerationTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        updateConfig();

        dataManager = new ChunkDataManager(this);
        dataManager.load();

        blockPool = new BlockPool(this);

        getServer().getPluginManager().registerEvents(new ChunkEnterListener(this), this);

        RandomChunksCommand cmd = new RandomChunksCommand(this);
        getCommand("randomchunks").setExecutor(cmd);
        getCommand("randomchunks").setTabCompleter(cmd);

        getLogger().info("RandomChunks activé – " + blockPool.size() + " blocs dans le pool.");
    }

    @Override
    public void onDisable() {
        if (pregenerationTask != null) {
            pregenerationTask.cancel();
            pregenerationTask = null;
        }
        ChunkTransformer.clearCache();
        if (dataManager != null) dataManager.save();
        getLogger().info("RandomChunks désactivé.");
    }

    public void updateConfig() {
        FileConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(getResource("config.yml"), StandardCharsets.UTF_8));
        FileConfiguration current = getConfig();
        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (!current.contains(key)) {
                current.set(key, defaults.get(key));
                changed = true;
                getLogger().info("config.yml : clé ajoutée — " + key);
            }
        }
        if (changed) saveConfig();
    }

    public ChunkDataManager getDataManager() { return dataManager; }
    public BlockPool getBlockPool() { return blockPool; }
    public PregenerationTask getPregenerationTask() { return pregenerationTask; }
    public void setPregenerationTask(PregenerationTask task) { this.pregenerationTask = task; }
}
