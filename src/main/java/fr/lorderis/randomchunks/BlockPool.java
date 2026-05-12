package fr.lorderis.randomchunks;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BlockPool {

    private static final Set<Material> EXCLUDED = EnumSet.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.WATER, Material.LAVA,
            Material.BEDROCK,
            Material.BARRIER, Material.LIGHT, Material.STRUCTURE_VOID, Material.STRUCTURE_BLOCK,
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.JIGSAW,
            Material.MOVING_PISTON, Material.PISTON_HEAD,
            Material.KELP_PLANT, Material.BAMBOO_SAPLING,
            Material.SWEET_BERRY_BUSH,
            Material.TALL_SEAGRASS, Material.FROSTED_ICE,
            Material.FIRE, Material.SOUL_FIRE,
            Material.NETHER_PORTAL, Material.END_PORTAL, Material.END_GATEWAY,
            Material.RAIL, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL,
            Material.CARPET, Material.WHITE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET,
            Material.LIGHT_BLUE_CARPET, Material.YELLOW_CARPET, Material.LIME_CARPET,
            Material.PINK_CARPET, Material.GRAY_CARPET, Material.LIGHT_GRAY_CARPET,
            Material.CYAN_CARPET, Material.PURPLE_CARPET, Material.BLUE_CARPET,
            Material.BROWN_CARPET, Material.GREEN_CARPET, Material.RED_CARPET, Material.BLACK_CARPET,
            Material.MOSS_CARPET,
            Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON,
            Material.MANGROVE_BUTTON, Material.CHERRY_BUTTON, Material.BAMBOO_BUTTON,
            Material.CRIMSON_BUTTON, Material.WARPED_BUTTON,
            Material.STONE_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON,
            Material.LEVER
    );

    private final List<Material> pool = new ArrayList<>();
    private final Random random = new Random();

    public BlockPool(RandomChunksPlugin plugin) {
        List<String> configured = plugin.getConfig().getStringList("block-pool");

        if (!configured.isEmpty()) {
            for (String name : configured) {
                Material mat = Material.matchMaterial(name.toUpperCase());
                if (mat != null && mat.isBlock() && !EXCLUDED.contains(mat)) {
                    pool.add(mat);
                } else {
                    plugin.getLogger().warning("Bloc inconnu ou invalide dans block-pool : " + name);
                }
            }
        } else {
            for (Material mat : Material.values()) {
                if (mat.isBlock() && !mat.isAir() && !EXCLUDED.contains(mat)) {
                    pool.add(mat);
                }
            }
        }

        if (pool.isEmpty()) {
            throw new IllegalStateException("[RandomChunks] Le pool de blocs est vide — vérifiez config.yml.");
        }
    }

    public Material random() {
        return pool.get(random.nextInt(pool.size()));
    }

    public int size() {
        return pool.size();
    }
}
