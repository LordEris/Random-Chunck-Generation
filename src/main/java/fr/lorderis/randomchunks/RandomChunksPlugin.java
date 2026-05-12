package fr.lorderis.randomchunks;

import fr.lorderis.randomchunks.command.RandomChunksCommand;
import fr.lorderis.randomchunks.data.ChunkDataManager;
import fr.lorderis.randomchunks.listener.ChunkEnterListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomChunksPlugin extends JavaPlugin {

    private ChunkDataManager dataManager;
    private BlockPool blockPool;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
        if (dataManager != null) {
            dataManager.save();
        }
        getLogger().info("RandomChunks désactivé.");
    }

    public ChunkDataManager getDataManager() {
        return dataManager;
    }

    public BlockPool getBlockPool() {
        return blockPool;
    }
}
