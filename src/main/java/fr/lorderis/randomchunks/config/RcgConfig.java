package fr.lorderis.randomchunks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.lorderis.randomchunks.RandomChunks;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JSON config, written to {@code config/random-chunks.json} on first launch.
 *
 * <p>Keys added by a later version of the mod are appended to an existing file on load, with
 * their default value, so a config never silently misses a setting.
 */
public final class RcgConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String FILE_NAME = "random-chunks.json";

    /**
     * Blocks never picked when {@link #blockPool} is empty. Tags ({@code #minecraft:...}) and ids
     * are both accepted. The list only has to cover what looks wrong as a whole chunk (plants,
     * thin blocks, technical blocks): air, fluids, blocks with a block entity, blocks that fall and
     * blocks with no item form are always refused and need no entry here.
     */
    public static final List<String> DEFAULT_EXCLUDED_BLOCKS = List.of(
            // Technical, or dangerous as a whole chunk: one spark next to a lava lake would
            // detonate tens of thousands of TNT at once.
            "minecraft:bedrock",
            "minecraft:barrier",
            "minecraft:tnt",
            // Plants. #replaceable covers grass, ferns, bushes, dry grass, leaf litter, vines,
            // glow lichen, resin clumps, roots and snow layers.
            "#minecraft:replaceable",
            "#minecraft:saplings",
            "#minecraft:small_flowers",
            "minecraft:sunflower",
            "minecraft:lilac",
            "minecraft:rose_bush",
            "minecraft:peony",
            "minecraft:pitcher_plant",
            "minecraft:pink_petals",
            "minecraft:wildflowers",
            "minecraft:cactus_flower",
            "minecraft:firefly_bush",
            "minecraft:brown_mushroom",
            "minecraft:red_mushroom",
            "minecraft:crimson_fungus",
            "minecraft:warped_fungus",
            "minecraft:shelf_mushroom",
            "minecraft:cactus",
            "minecraft:sugar_cane",
            "minecraft:bamboo",
            "minecraft:sweet_berry_bush",
            "minecraft:lily_pad",
            "minecraft:spore_blossom",
            "minecraft:big_dripleaf",
            "minecraft:small_dripleaf",
            "minecraft:chorus_plant",
            "minecraft:chorus_flower",
            "#minecraft:crops",
            "minecraft:nether_wart",
            "minecraft:cocoa",
            "#minecraft:climbable",
            "minecraft:pale_hanging_moss",
            "minecraft:sculk_vein",
            // Moss
            "minecraft:moss_block",
            "minecraft:moss_carpet",
            "minecraft:pale_moss_block",
            "minecraft:pale_moss_carpet",
            "minecraft:mossy_cobblestone",
            "minecraft:mossy_cobblestone_slab",
            "minecraft:mossy_cobblestone_stairs",
            "minecraft:mossy_stone_bricks",
            "minecraft:mossy_stone_brick_slab",
            "minecraft:mossy_stone_brick_stairs",
            // Thin, partial or multi-block shapes
            "#minecraft:fences",
            "#minecraft:fence_gates",
            "#minecraft:walls",
            "#minecraft:doors",
            "#minecraft:trapdoors",
            "#minecraft:beds",
            "minecraft:straw_bed",
            "#minecraft:wool_carpets",
            "#minecraft:rails",
            "#minecraft:buttons",
            "#minecraft:pressure_plates",
            "#minecraft:candles",
            "minecraft:cake",
            "#minecraft:flower_pots",
            "#minecraft:bars",
            "#minecraft:chains",
            "#minecraft:lanterns",
            "#minecraft:lightning_rods",
            "minecraft:glass_pane",
            "minecraft:white_stained_glass_pane",
            "minecraft:orange_stained_glass_pane",
            "minecraft:magenta_stained_glass_pane",
            "minecraft:light_blue_stained_glass_pane",
            "minecraft:yellow_stained_glass_pane",
            "minecraft:lime_stained_glass_pane",
            "minecraft:pink_stained_glass_pane",
            "minecraft:gray_stained_glass_pane",
            "minecraft:light_gray_stained_glass_pane",
            "minecraft:cyan_stained_glass_pane",
            "minecraft:purple_stained_glass_pane",
            "minecraft:blue_stained_glass_pane",
            "minecraft:brown_stained_glass_pane",
            "minecraft:green_stained_glass_pane",
            "minecraft:red_stained_glass_pane",
            "minecraft:black_stained_glass_pane",
            "minecraft:copper_grate",
            "minecraft:exposed_copper_grate",
            "minecraft:weathered_copper_grate",
            "minecraft:oxidized_copper_grate",
            "minecraft:waxed_copper_grate",
            "minecraft:waxed_exposed_copper_grate",
            "minecraft:waxed_weathered_copper_grate",
            "minecraft:waxed_oxidized_copper_grate",
            "minecraft:torch",
            "minecraft:soul_torch",
            "minecraft:redstone_torch",
            "minecraft:copper_torch",
            "minecraft:end_rod",
            "minecraft:lever",
            "minecraft:tripwire_hook",
            "minecraft:tripwire",
            "minecraft:redstone_wire",
            "minecraft:repeater",
            "minecraft:small_amethyst_bud",
            "minecraft:medium_amethyst_bud",
            "minecraft:large_amethyst_bud",
            "minecraft:amethyst_cluster",
            "minecraft:grindstone",
            "minecraft:heavy_core",
            "minecraft:dried_ghast"
    );

    /** Master switch. When false the mod does nothing at all. */
    public boolean enabled = true;

    /**
     * Dimensions whose new chunks are filled with a random block. The single entry {@code "*"}
     * targets every dimension, modded ones included.
     */
    public List<String> dimensions = new ArrayList<>(List.of("*"));

    /**
     * When non-empty, the only blocks that can be picked (ids or {@code #tags}). They still go
     * through {@link #excludedBlocks} and the built-in safety rules. Empty means every block the
     * game knows, minus the exclusions.
     */
    public List<String> blockPool = new ArrayList<>();

    /** Blocks never picked. See {@link #DEFAULT_EXCLUDED_BLOCKS}. */
    public List<String> excludedBlocks = new ArrayList<>(DEFAULT_EXCLUDED_BLOCKS);

    /** Leave bedrock where it is, so the world keeps its floor and the Nether its ceiling. */
    public boolean preserveBedrock = true;

    /** Lowest Y that is rewritten. Clamped to the bottom of each dimension. */
    public int minY = -64;

    /** Highest Y that is rewritten. Clamped to the top of each dimension. */
    public int maxY = 320;

    /**
     * Chunks within this many chunks of the world spawn are left as vanilla generated them, so a
     * new player does not start inside a solid block. 0 disables the protection.
     */
    public int spawnProtectionRadius = 2;

    /** Chunk requests sent per server tick by {@code /randomchunks pregen}. */
    public int pregenChunksPerTick = 10;

    // Volatile: chunks are generated on several worker threads, and a set published through a plain
    // field could be seen half-built.
    private transient volatile Set<String> dimensionCache;

    public boolean appliesToDimension(Identifier dimension) {
        Set<String> cache = this.dimensionCache;
        if (cache == null) {
            cache = new HashSet<>();
            for (String entry : this.dimensions) {
                String normalized = normalizeId(entry);
                if (normalized != null) {
                    cache.add(normalized);
                }
            }
            this.dimensionCache = cache;
        }
        return cache.contains("*") || cache.contains(dimension.toString());
    }

    /**
     * Lower-cases an id and gives it the {@code minecraft} namespace when it has none, keeping a
     * leading {@code #}. Accepts the upper-case names of the old Spigot config ({@code STONE}).
     */
    public static String normalizeId(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim().toLowerCase(java.util.Locale.ROOT);
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.equals("*")) {
            return trimmed;
        }
        boolean tag = trimmed.startsWith("#");
        String id = tag ? trimmed.substring(1) : trimmed;
        if (id.indexOf(':') < 0) {
            id = "minecraft:" + id;
        }
        return tag ? "#" + id : id;
    }

    /** Gson leaves a field null when the file sets it to null; fall back to the defaults. */
    private RcgConfig sanitize() {
        RcgConfig defaults = new RcgConfig();
        if (this.dimensions == null) {
            this.dimensions = defaults.dimensions;
        }
        if (this.blockPool == null) {
            this.blockPool = defaults.blockPool;
        }
        if (this.excludedBlocks == null) {
            this.excludedBlocks = defaults.excludedBlocks;
        }
        if (this.minY > this.maxY) {
            RandomChunks.LOGGER.warn("minY ({}) is above maxY ({}), swapping them.", this.minY, this.maxY);
            int swap = this.minY;
            this.minY = this.maxY;
            this.maxY = swap;
        }
        if (this.pregenChunksPerTick < 1) {
            this.pregenChunksPerTick = 1;
        }
        return this;
    }

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public static RcgConfig loadOrCreate() {
        Path path = path();
        if (!Files.isRegularFile(path)) {
            RcgConfig defaults = new RcgConfig();
            write(path, defaults);
            return defaults;
        }

        JsonObject raw;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                RandomChunks.LOGGER.warn("{} does not hold a JSON object, falling back to the defaults.", FILE_NAME);
                return new RcgConfig();
            }
            raw = parsed.getAsJsonObject();
        } catch (IOException | JsonParseException | IllegalStateException e) {
            RandomChunks.LOGGER.error("Could not read {}, falling back to the defaults.", FILE_NAME, e);
            return new RcgConfig();
        }

        RcgConfig loaded;
        try {
            loaded = GSON.fromJson(raw, RcgConfig.class);
        } catch (JsonParseException e) {
            RandomChunks.LOGGER.error("{} has a value of the wrong type, falling back to the defaults.", FILE_NAME, e);
            return new RcgConfig();
        }
        loaded.sanitize();

        // Same service as the plugin's updateConfig(): add what a newer version introduced, keep
        // everything the user already set.
        boolean missingKeys = false;
        for (String key : GSON.toJsonTree(new RcgConfig()).getAsJsonObject().keySet()) {
            if (!raw.has(key)) {
                RandomChunks.LOGGER.info("{}: added missing key '{}'", FILE_NAME, key);
                missingKeys = true;
            }
        }
        if (missingKeys) {
            write(path, loaded);
        }
        return loaded;
    }

    private static void write(Path path, RcgConfig config) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            RandomChunks.LOGGER.error("Could not write {}.", FILE_NAME, e);
        }
    }
}
