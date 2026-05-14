package fr.lorderis.randomchunks;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.List;
import java.util.Random;

public class RandomChunksPopulator extends BlockPopulator {

    private final RandomChunksPlugin plugin;

    public RandomChunksPopulator(RandomChunksPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(world.getName())) return;
        if (ChunkTransformer.isProtectedSpawn(plugin, world, chunk.getX(), chunk.getZ())) return;
        if (plugin.getDataManager().isTransformed(world.getName(), chunk.getX(), chunk.getZ())) return;

        plugin.getDataManager().markTransformed(world.getName(), chunk.getX(), chunk.getZ());
        ChunkTransformer.transform(plugin, world, chunk);
    }
}
