package fr.lorderis.randomchunks;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BlockPool {

    private static final Set<Material> EXCLUDED;
    static {
        EXCLUDED = EnumSet.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.WATER, Material.LAVA,
            Material.BEDROCK,
            Material.BARRIER, Material.LIGHT, Material.STRUCTURE_VOID, Material.STRUCTURE_BLOCK,
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.JIGSAW,
            Material.MOVING_PISTON, Material.PISTON_HEAD,
            Material.KELP_PLANT, Material.KELP, Material.BAMBOO_SAPLING, Material.BAMBOO,
            Material.SWEET_BERRY_BUSH, Material.CACTUS, Material.SUGAR_CANE,
            Material.SHORT_GRASS, Material.FERN, Material.DEAD_BUSH,
            Material.TALL_GRASS, Material.LARGE_FERN,
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP,
            Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY,
            Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE,
            Material.TORCHFLOWER, Material.PITCHER_PLANT,
            Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY,
            Material.PINK_PETALS,
            Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING,
            Material.CHERRY_SAPLING, Material.BAMBOO_SAPLING, Material.MANGROVE_PROPAGULE,
            Material.AZALEA, Material.FLOWERING_AZALEA,
            Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
            Material.LILY_PAD, Material.SEAGRASS, Material.SEA_PICKLE,
            Material.VINE, Material.GLOW_LICHEN, Material.HANGING_ROOTS,
            Material.CAVE_VINES, Material.CAVE_VINES_PLANT,
            Material.TWISTING_VINES, Material.TWISTING_VINES_PLANT,
            Material.WEEPING_VINES, Material.WEEPING_VINES_PLANT,
            Material.NETHER_SPROUTS, Material.CRIMSON_ROOTS, Material.WARPED_ROOTS,
            Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS,
            Material.SPORE_BLOSSOM, Material.BIG_DRIPLEAF, Material.BIG_DRIPLEAF_STEM,
            Material.SMALL_DRIPLEAF,
            Material.CHORUS_PLANT, Material.CHORUS_FLOWER,
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
            Material.MELON_STEM, Material.PUMPKIN_STEM,
            Material.ATTACHED_MELON_STEM, Material.ATTACHED_PUMPKIN_STEM,
            Material.NETHER_WART, Material.COCOA,
            Material.TORCHFLOWER_CROP, Material.PITCHER_CROP,
            Material.TUBE_CORAL, Material.BRAIN_CORAL, Material.BUBBLE_CORAL,
            Material.FIRE_CORAL, Material.HORN_CORAL,
            Material.TUBE_CORAL_FAN, Material.BRAIN_CORAL_FAN, Material.BUBBLE_CORAL_FAN,
            Material.FIRE_CORAL_FAN, Material.HORN_CORAL_FAN,
            Material.TUBE_CORAL_WALL_FAN, Material.BRAIN_CORAL_WALL_FAN,
            Material.BUBBLE_CORAL_WALL_FAN, Material.FIRE_CORAL_WALL_FAN,
            Material.HORN_CORAL_WALL_FAN,
            Material.DEAD_TUBE_CORAL, Material.DEAD_BRAIN_CORAL, Material.DEAD_BUBBLE_CORAL,
            Material.DEAD_FIRE_CORAL, Material.DEAD_HORN_CORAL,
            Material.DEAD_TUBE_CORAL_FAN, Material.DEAD_BRAIN_CORAL_FAN,
            Material.DEAD_BUBBLE_CORAL_FAN, Material.DEAD_FIRE_CORAL_FAN,
            Material.DEAD_HORN_CORAL_FAN,
            Material.DEAD_TUBE_CORAL_WALL_FAN, Material.DEAD_BRAIN_CORAL_WALL_FAN,
            Material.DEAD_BUBBLE_CORAL_WALL_FAN, Material.DEAD_FIRE_CORAL_WALL_FAN,
            Material.DEAD_HORN_CORAL_WALL_FAN,
            Material.TALL_SEAGRASS, Material.FROSTED_ICE,
            Material.FIRE, Material.SOUL_FIRE,
            Material.NETHER_PORTAL, Material.END_PORTAL, Material.END_GATEWAY,
            Material.RAIL, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL,
            Material.WHITE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET,
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
            Material.LEVER,
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
            Material.BARREL, Material.HOPPER, Material.DISPENSER, Material.DROPPER,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.BREWING_STAND,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.SPAWNER, Material.TRIAL_SPAWNER, Material.VAULT, Material.CRAFTER, Material.HEAVY_CORE,
            Material.OAK_SIGN, Material.SPRUCE_SIGN, Material.BIRCH_SIGN, Material.JUNGLE_SIGN,
            Material.ACACIA_SIGN, Material.DARK_OAK_SIGN, Material.MANGROVE_SIGN,
            Material.CHERRY_SIGN, Material.BAMBOO_SIGN, Material.CRIMSON_SIGN, Material.WARPED_SIGN,
            Material.OAK_WALL_SIGN, Material.SPRUCE_WALL_SIGN, Material.BIRCH_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN, Material.ACACIA_WALL_SIGN, Material.DARK_OAK_WALL_SIGN,
            Material.MANGROVE_WALL_SIGN, Material.CHERRY_WALL_SIGN, Material.BAMBOO_WALL_SIGN,
            Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN,
            Material.OAK_HANGING_SIGN, Material.SPRUCE_HANGING_SIGN, Material.BIRCH_HANGING_SIGN,
            Material.JUNGLE_HANGING_SIGN, Material.ACACIA_HANGING_SIGN, Material.DARK_OAK_HANGING_SIGN,
            Material.MANGROVE_HANGING_SIGN, Material.CHERRY_HANGING_SIGN, Material.BAMBOO_HANGING_SIGN,
            Material.CRIMSON_HANGING_SIGN, Material.WARPED_HANGING_SIGN,
            Material.OAK_WALL_HANGING_SIGN, Material.SPRUCE_WALL_HANGING_SIGN, Material.BIRCH_WALL_HANGING_SIGN,
            Material.JUNGLE_WALL_HANGING_SIGN, Material.ACACIA_WALL_HANGING_SIGN, Material.DARK_OAK_WALL_HANGING_SIGN,
            Material.MANGROVE_WALL_HANGING_SIGN, Material.CHERRY_WALL_HANGING_SIGN, Material.BAMBOO_WALL_HANGING_SIGN,
            Material.CRIMSON_WALL_HANGING_SIGN, Material.WARPED_WALL_HANGING_SIGN,
            Material.SKELETON_SKULL, Material.SKELETON_WALL_SKULL,
            Material.WITHER_SKELETON_SKULL, Material.WITHER_SKELETON_WALL_SKULL,
            Material.ZOMBIE_HEAD, Material.ZOMBIE_WALL_HEAD,
            Material.PLAYER_HEAD, Material.PLAYER_WALL_HEAD,
            Material.CREEPER_HEAD, Material.CREEPER_WALL_HEAD,
            Material.DRAGON_HEAD, Material.DRAGON_WALL_HEAD,
            Material.PIGLIN_HEAD, Material.PIGLIN_WALL_HEAD,
            Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER,
            Material.LIGHT_BLUE_BANNER, Material.YELLOW_BANNER, Material.LIME_BANNER,
            Material.PINK_BANNER, Material.GRAY_BANNER, Material.LIGHT_GRAY_BANNER,
            Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER,
            Material.BROWN_BANNER, Material.GREEN_BANNER, Material.RED_BANNER, Material.BLACK_BANNER,
            Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER, Material.MAGENTA_WALL_BANNER,
            Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER, Material.LIME_WALL_BANNER,
            Material.PINK_WALL_BANNER, Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER,
            Material.CYAN_WALL_BANNER, Material.PURPLE_WALL_BANNER, Material.BLUE_WALL_BANNER,
            Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER, Material.RED_WALL_BANNER, Material.BLACK_WALL_BANNER,
            Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.BEACON, Material.JUKEBOX, Material.LECTERN,
            Material.CHISELED_BOOKSHELF, Material.DECORATED_POT,
            Material.BEE_NEST, Material.BEEHIVE,
            Material.SCULK_SENSOR, Material.CALIBRATED_SCULK_SENSOR,
            Material.SCULK_SHRIEKER, Material.SCULK_CATALYST, Material.SCULK_VEIN,
            Material.SUSPICIOUS_SAND, Material.SUSPICIOUS_GRAVEL,
            Material.COMPARATOR,
            Material.TORCH, Material.WALL_TORCH,
            Material.SOUL_TORCH, Material.SOUL_WALL_TORCH,
            Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH,
            Material.LANTERN, Material.SOUL_LANTERN,
            Material.CANDLE,
            Material.WHITE_CANDLE, Material.ORANGE_CANDLE, Material.MAGENTA_CANDLE,
            Material.LIGHT_BLUE_CANDLE, Material.YELLOW_CANDLE, Material.LIME_CANDLE,
            Material.PINK_CANDLE, Material.GRAY_CANDLE, Material.LIGHT_GRAY_CANDLE,
            Material.CYAN_CANDLE, Material.PURPLE_CANDLE, Material.BLUE_CANDLE,
            Material.BROWN_CANDLE, Material.GREEN_CANDLE, Material.RED_CANDLE, Material.BLACK_CANDLE,
            Material.REDSTONE_WIRE,
            Material.REPEATER,
            Material.TRIPWIRE, Material.TRIPWIRE_HOOK,
            Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE,
            Material.MANGROVE_PRESSURE_PLATE, Material.CHERRY_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Material.IRON_BARS,
            Material.COPPER_GRATE, Material.EXPOSED_COPPER_GRATE,
            Material.WEATHERED_COPPER_GRATE, Material.OXIDIZED_COPPER_GRATE,
            Material.WAXED_COPPER_GRATE, Material.WAXED_EXPOSED_COPPER_GRATE,
            Material.WAXED_WEATHERED_COPPER_GRATE, Material.WAXED_OXIDIZED_COPPER_GRATE,
            Material.SAND, Material.RED_SAND,
            Material.GRAVEL,
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
            Material.WHITE_CONCRETE_POWDER, Material.ORANGE_CONCRETE_POWDER,
            Material.MAGENTA_CONCRETE_POWDER, Material.LIGHT_BLUE_CONCRETE_POWDER,
            Material.YELLOW_CONCRETE_POWDER, Material.LIME_CONCRETE_POWDER,
            Material.PINK_CONCRETE_POWDER, Material.GRAY_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE_POWDER, Material.CYAN_CONCRETE_POWDER,
            Material.PURPLE_CONCRETE_POWDER, Material.BLUE_CONCRETE_POWDER,
            Material.BROWN_CONCRETE_POWDER, Material.GREEN_CONCRETE_POWDER,
            Material.RED_CONCRETE_POWDER, Material.BLACK_CONCRETE_POWDER,
            Material.DRAGON_EGG,
            Material.POINTED_DRIPSTONE,
            Material.SCAFFOLDING,
            Material.FLOWER_POT,
            Material.POTTED_OAK_SAPLING, Material.POTTED_SPRUCE_SAPLING,
            Material.POTTED_BIRCH_SAPLING, Material.POTTED_JUNGLE_SAPLING,
            Material.POTTED_ACACIA_SAPLING, Material.POTTED_DARK_OAK_SAPLING,
            Material.POTTED_FERN, Material.POTTED_DEAD_BUSH, Material.POTTED_CACTUS,
            Material.POTTED_DANDELION, Material.POTTED_POPPY,
            Material.POTTED_BLUE_ORCHID, Material.POTTED_ALLIUM,
            Material.POTTED_AZURE_BLUET, Material.POTTED_RED_TULIP,
            Material.POTTED_ORANGE_TULIP, Material.POTTED_WHITE_TULIP,
            Material.POTTED_PINK_TULIP, Material.POTTED_OXEYE_DAISY,
            Material.POTTED_CORNFLOWER, Material.POTTED_LILY_OF_THE_VALLEY,
            Material.POTTED_WITHER_ROSE,
            Material.POTTED_RED_MUSHROOM, Material.POTTED_BROWN_MUSHROOM,
            Material.POTTED_CRIMSON_FUNGUS, Material.POTTED_WARPED_FUNGUS,
            Material.POTTED_CRIMSON_ROOTS, Material.POTTED_WARPED_ROOTS,
            Material.POTTED_AZALEA_BUSH, Material.POTTED_FLOWERING_AZALEA_BUSH,
            Material.POTTED_BAMBOO, Material.POTTED_TORCHFLOWER,
            Material.POTTED_CHERRY_SAPLING, Material.POTTED_MANGROVE_PROPAGULE
        );
        // Chiseled bookshelf (bloc avec inventaire)
        EXCLUDED.add(Material.CHISELED_BOOKSHELF);
        // Ladders
        EXCLUDED.add(Material.LADDER);
        // Bell
        EXCLUDED.add(Material.BELL);
        // Amethyst buds & cluster
        EXCLUDED.addAll(EnumSet.of(
            Material.SMALL_AMETHYST_BUD, Material.MEDIUM_AMETHYST_BUD,
            Material.LARGE_AMETHYST_BUD, Material.AMETHYST_CLUSTER
        ));
        // Daylight detector
        EXCLUDED.add(Material.DAYLIGHT_DETECTOR);
        // Walls
        EXCLUDED.addAll(EnumSet.of(
            Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL,
            Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL,
            Material.ANDESITE_WALL, Material.DIORITE_WALL, Material.GRANITE_WALL,
            Material.BRICK_WALL, Material.PRISMARINE_WALL,
            Material.RED_SANDSTONE_WALL, Material.SANDSTONE_WALL,
            Material.END_STONE_BRICK_WALL,
            Material.NETHER_BRICK_WALL, Material.RED_NETHER_BRICK_WALL,
            Material.BLACKSTONE_WALL, Material.POLISHED_BLACKSTONE_WALL,
            Material.POLISHED_BLACKSTONE_BRICK_WALL,
            Material.DEEPSLATE_TILE_WALL, Material.DEEPSLATE_BRICK_WALL,
            Material.COBBLED_DEEPSLATE_WALL, Material.POLISHED_DEEPSLATE_WALL,
            Material.MUD_BRICK_WALL,
            Material.TUFF_WALL, Material.TUFF_BRICK_WALL, Material.POLISHED_TUFF_WALL
        ));
        // Glass panes
        EXCLUDED.addAll(EnumSet.of(
            Material.GLASS_PANE,
            Material.WHITE_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE,
            Material.LIGHT_GRAY_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
            Material.BROWN_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE,
            Material.RED_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE
        ));
        // Fences
        EXCLUDED.addAll(EnumSet.of(
            Material.OAK_FENCE, Material.SPRUCE_FENCE, Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE, Material.ACACIA_FENCE, Material.DARK_OAK_FENCE,
            Material.MANGROVE_FENCE, Material.CHERRY_FENCE, Material.BAMBOO_FENCE,
            Material.CRIMSON_FENCE, Material.WARPED_FENCE, Material.NETHER_BRICK_FENCE
        ));
        // Fence gates
        EXCLUDED.addAll(EnumSet.of(
            Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE,
            Material.MANGROVE_FENCE_GATE, Material.CHERRY_FENCE_GATE, Material.BAMBOO_FENCE_GATE,
            Material.CRIMSON_FENCE_GATE, Material.WARPED_FENCE_GATE
        ));
        // Doors
        EXCLUDED.addAll(EnumSet.of(
            Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR,
            Material.MANGROVE_DOOR, Material.CHERRY_DOOR, Material.BAMBOO_DOOR,
            Material.CRIMSON_DOOR, Material.WARPED_DOOR, Material.IRON_DOOR,
            Material.COPPER_DOOR, Material.EXPOSED_COPPER_DOOR,
            Material.WEATHERED_COPPER_DOOR, Material.OXIDIZED_COPPER_DOOR,
            Material.WAXED_COPPER_DOOR, Material.WAXED_EXPOSED_COPPER_DOOR,
            Material.WAXED_WEATHERED_COPPER_DOOR, Material.WAXED_OXIDIZED_COPPER_DOOR
        ));
        // Trapdoors
        EXCLUDED.addAll(EnumSet.of(
            Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR, Material.ACACIA_TRAPDOOR, Material.DARK_OAK_TRAPDOOR,
            Material.MANGROVE_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.BAMBOO_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR, Material.IRON_TRAPDOOR,
            Material.COPPER_TRAPDOOR, Material.EXPOSED_COPPER_TRAPDOOR,
            Material.WEATHERED_COPPER_TRAPDOOR, Material.OXIDIZED_COPPER_TRAPDOOR,
            Material.WAXED_COPPER_TRAPDOOR, Material.WAXED_EXPOSED_COPPER_TRAPDOOR,
            Material.WAXED_WEATHERED_COPPER_TRAPDOOR, Material.WAXED_OXIDIZED_COPPER_TRAPDOOR
        ));
        // Beds
        EXCLUDED.addAll(EnumSet.of(
            Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED, Material.YELLOW_BED, Material.LIME_BED,
            Material.PINK_BED, Material.GRAY_BED, Material.LIGHT_GRAY_BED,
            Material.CYAN_BED, Material.PURPLE_BED, Material.BLUE_BED,
            Material.BROWN_BED, Material.GREEN_BED, Material.RED_BED, Material.BLACK_BED
        ));
        // Cakes
        EXCLUDED.addAll(EnumSet.of(
            Material.CAKE,
            Material.CANDLE_CAKE,
            Material.WHITE_CANDLE_CAKE, Material.ORANGE_CANDLE_CAKE,
            Material.MAGENTA_CANDLE_CAKE, Material.LIGHT_BLUE_CANDLE_CAKE,
            Material.YELLOW_CANDLE_CAKE, Material.LIME_CANDLE_CAKE,
            Material.PINK_CANDLE_CAKE, Material.GRAY_CANDLE_CAKE,
            Material.LIGHT_GRAY_CANDLE_CAKE, Material.CYAN_CANDLE_CAKE,
            Material.PURPLE_CANDLE_CAKE, Material.BLUE_CANDLE_CAKE,
            Material.BROWN_CANDLE_CAKE, Material.GREEN_CANDLE_CAKE,
            Material.RED_CANDLE_CAKE, Material.BLACK_CANDLE_CAKE
        ));
        // Moss
        EXCLUDED.addAll(EnumSet.of(
            Material.MOSS_BLOCK, Material.MOSS_CARPET,
            Material.MOSSY_COBBLESTONE, Material.MOSSY_COBBLESTONE_SLAB,
            Material.MOSSY_COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_WALL,
            Material.MOSSY_STONE_BRICKS, Material.MOSSY_STONE_BRICK_SLAB,
            Material.MOSSY_STONE_BRICK_STAIRS, Material.MOSSY_STONE_BRICK_WALL
        ));
        for (String name : new String[]{
            "CHAIN",
            "LIGHTNING_ROD",
            // Pale Oak (1.21.4+)
            "PALE_OAK_FENCE", "PALE_OAK_FENCE_GATE",
            "PALE_OAK_SIGN", "PALE_OAK_WALL_SIGN",
            "PALE_OAK_HANGING_SIGN", "PALE_OAK_WALL_HANGING_SIGN",
            "PALE_HANGING_MOSS", "PALE_MOSS_BLOCK", "PALE_MOSS_CARPET",
            // Copper Golem statues (toutes les variantes d'oxydation)
            "COPPER_GOLEM_STATUE",
            "EXPOSED_COPPER_GOLEM_STATUE",
            "WEATHERED_COPPER_GOLEM_STATUE",
            "OXIDIZED_COPPER_GOLEM_STATUE",
            "WAXED_COPPER_GOLEM_STATUE",
            "WAXED_EXPOSED_COPPER_GOLEM_STATUE",
            "WAXED_WEATHERED_COPPER_GOLEM_STATUE",
            "WAXED_OXIDIZED_COPPER_GOLEM_STATUE",
            // Copper chains (toutes versions d'oxydation)
            "COPPER_CHAIN", "EXPOSED_COPPER_CHAIN",
            "WEATHERED_COPPER_CHAIN", "OXIDIZED_COPPER_CHAIN",
            "WAXED_COPPER_CHAIN", "WAXED_EXPOSED_COPPER_CHAIN",
            "WAXED_WEATHERED_COPPER_CHAIN", "WAXED_OXIDIZED_COPPER_CHAIN",
            // Blocs admin supplémentaires (26.1.2+)
            "TEST_BLOCK", "TEST_INSTANCE_BLOCK",
            // Ladders supplémentaires (26.1.2+)
            "BAMBOO_LADDER", "OAK_LADDER", "SPRUCE_LADDER", "BIRCH_LADDER",
            "JUNGLE_LADDER", "ACACIA_LADDER", "DARK_OAK_LADDER",
            "MANGROVE_LADDER", "CHERRY_LADDER", "CRIMSON_LADDER", "WARPED_LADDER",
            // Flower pots supplémentaires (26.1.2+)
            "POTTED_PALE_OAK_SAPLING", "POTTED_EYEBLOSSOM",
            "POTTED_OPEN_EYEBLOSSOM", "POTTED_CLOSED_EYEBLOSSOM",
            "POTTED_WILDFLOWERS", "POTTED_CACTUS_FLOWER",
            "POTTED_SHORT_DRY_GRASS", "POTTED_DRY_BUSH",
            // Cactus flower (26.1.2+)
            "CACTUS_FLOWER",
            // Nouvelles plantes (26.1.2+)
            "GOLDEN_DANDELION", "POTTED_GOLDEN_DANDELION",
            "FIREFLY_BUSH",
            // Shelf (26.1.2+)
            "SHELF", "OAK_SHELF", "SPRUCE_SHELF", "BIRCH_SHELF",
            "JUNGLE_SHELF", "ACACIA_SHELF", "DARK_OAK_SHELF",
            "MANGROVE_SHELF", "CHERRY_SHELF", "BAMBOO_SHELF",
            "CRIMSON_SHELF", "WARPED_SHELF", "PALE_OAK_SHELF",
            // Dry grass (26.1.2+)
            "DRY_GRASS", "TALL_DRY_GRASS", "SHORT_DRY_GRASS",
            "DRY_BUSH", "DEAD_DRY_BUSH",
            // Petals supplémentaires (26.1.2+)
            "WHITE_PETALS", "ORANGE_PETALS", "MAGENTA_PETALS", "LIGHT_BLUE_PETALS",
            "YELLOW_PETALS", "LIME_PETALS", "GRAY_PETALS", "LIGHT_GRAY_PETALS",
            "CYAN_PETALS", "PURPLE_PETALS", "BLUE_PETALS", "BROWN_PETALS",
            "GREEN_PETALS", "RED_PETALS", "BLACK_PETALS",
            // Plantes supplémentaires (26.1.2+)
            "PALE_OAK_SAPLING", "EYEBLOSSOM", "OPEN_EYEBLOSSOM", "CLOSED_EYEBLOSSOM",
            "WILDFLOWERS", "LEAF_LITTER",
            // Lightning rod cuivre (toutes versions d'oxydation)
            "COPPER_LIGHTNING_ROD", "EXPOSED_COPPER_LIGHTNING_ROD",
            "WEATHERED_COPPER_LIGHTNING_ROD", "OXIDIZED_COPPER_LIGHTNING_ROD",
            "WAXED_COPPER_LIGHTNING_ROD", "WAXED_EXPOSED_COPPER_LIGHTNING_ROD",
            "WAXED_WEATHERED_COPPER_LIGHTNING_ROD", "WAXED_OXIDIZED_COPPER_LIGHTNING_ROD",
            // Copper bars (toutes versions d'oxydation)
            "COPPER_BARS", "EXPOSED_COPPER_BARS",
            "WEATHERED_COPPER_BARS", "OXIDIZED_COPPER_BARS",
            "WAXED_COPPER_BARS", "WAXED_EXPOSED_COPPER_BARS",
            "WAXED_WEATHERED_COPPER_BARS", "WAXED_OXIDIZED_COPPER_BARS",
            // Copper lanterns (toutes versions d'oxydation)
            "COPPER_LANTERN", "EXPOSED_COPPER_LANTERN",
            "WEATHERED_COPPER_LANTERN", "OXIDIZED_COPPER_LANTERN",
            "WAXED_COPPER_LANTERN", "WAXED_EXPOSED_COPPER_LANTERN",
            "WAXED_WEATHERED_COPPER_LANTERN", "WAXED_OXIDIZED_COPPER_LANTERN"
        }) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) EXCLUDED.add(mat);
        }
    }

    // Blocs non remplaçables lors du scan (air + fluides uniquement)
    private static final Set<Material> SCAN_EXCLUDED = EnumSet.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.WATER, Material.LAVA
    );

    private final Material[] pool;

    public BlockPool(RandomChunksPlugin plugin) {
        List<String> configured = plugin.getConfig().getStringList("block-pool");
        List<Material> list = new ArrayList<>();

        if (!configured.isEmpty()) {
            for (String name : configured) {
                Material mat = Material.matchMaterial(name.toUpperCase());
                if (mat != null && mat.isBlock() && !EXCLUDED.contains(mat)) {
                    list.add(mat);
                } else {
                    plugin.getLogger().warning("Bloc inconnu ou invalide dans block-pool : " + name);
                }
            }
        } else {
            for (Material mat : Material.values()) {
                if (mat.isBlock() && !mat.isAir() && !EXCLUDED.contains(mat)) {
                    list.add(mat);
                }
            }
        }

        if (list.isEmpty()) {
            throw new IllegalStateException("[RandomChunks] Le pool de blocs est vide — vérifiez config.yml.");
        }

        pool = list.toArray(new Material[0]);
    }

    public Material random() {
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }

    // Utilisé pour filtrer le pool (matériaux non choisissables)
    public boolean isExcluded(Material material) {
        return EXCLUDED.contains(material);
    }

    // Utilisé pendant le scan : seuls l'air et les fluides sont ignorés
    public boolean isScanExcluded(Material material) {
        return SCAN_EXCLUDED.contains(material);
    }

    public int size() {
        return pool.length;
    }
}
