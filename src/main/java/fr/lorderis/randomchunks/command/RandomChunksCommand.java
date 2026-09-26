package fr.lorderis.randomchunks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.lorderis.randomchunks.RandomChunks;
import fr.lorderis.randomchunks.config.RcgConfig;
import fr.lorderis.randomchunks.gen.BlockPool;
import fr.lorderis.randomchunks.pregen.PregenTask;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;

/**
 * {@code /randomchunks} (alias {@code /rcg}), for operators: reload, info and pregeneration.
 */
public final class RandomChunksCommand {
    private static final int MAX_RADIUS = 5000;

    private RandomChunksCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(Commands.literal("randomchunks")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(c -> help(c.getSource(), "randomchunks"))
                .then(Commands.literal("reload").executes(c -> reload(c.getSource())))
                .then(Commands.literal("info").executes(c -> info(c.getSource())))
                .then(Commands.literal("pregen")
                        .then(Commands.literal("start")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS))
                                                .executes(c -> startPregen(c, null))
                                                .then(Commands.argument("center", ColumnPosArgument.columnPos())
                                                        .executes(c -> startPregen(c, ColumnPosArgument.getColumnPos(c, "center")))))))
                        .then(Commands.literal("stop").executes(c -> stopPregen(c.getSource())))
                        .then(Commands.literal("status").executes(c -> pregenStatus(c.getSource())))));

        dispatcher.register(Commands.literal("rcg")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(c -> help(c.getSource(), "rcg"))
                .redirect(root));
    }

    private static int help(CommandSourceStack source, String label) {
        source.sendSuccess(() -> Component.literal("--- Random Chunks ---").withStyle(ChatFormatting.AQUA), false);
        line(source, label, "reload", "reload the config and rebuild the block pool");
        line(source, label, "info", "show the block pool and what has been generated");
        line(source, label, "pregen start <dimension> <radius> [x z]", "generate chunks ahead of time");
        line(source, label, "pregen stop", "cancel the running pregeneration");
        line(source, label, "pregen status", "show its progress");
        return 1;
    }

    private static void line(CommandSourceStack source, String label, String usage, String description) {
        source.sendSuccess(() -> Component.literal("/" + label + " " + usage).withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - " + description).withStyle(ChatFormatting.WHITE)), false);
    }

    private static int reload(CommandSourceStack source) {
        BlockPool pool = RandomChunks.reload();
        for (String warning : pool.warnings()) {
            source.sendFailure(Component.literal(warning));
        }
        if (pool.isEmpty()) {
            source.sendFailure(Component.literal("[Random Chunks] The block pool is empty: new chunks will not be changed."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[Random Chunks] Config reloaded, " + pool.size() + " blocks in the pool.")
                .withStyle(ChatFormatting.GREEN), true);
        return pool.size();
    }

    private static int info(CommandSourceStack source) {
        RcgConfig config = RandomChunks.config();
        BlockPool pool = RandomChunks.pool();
        source.sendSuccess(() -> Component.literal("[Random Chunks] ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(config.enabled ? "enabled" : "disabled in the config")
                        .withStyle(config.enabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
        source.sendSuccess(() -> Component.literal("Dimensions: " + String.join(", ", config.dimensions)), false);
        source.sendSuccess(() -> Component.literal("Blocks in the pool: " + pool.size()), false);
        source.sendSuccess(() -> Component.literal("Chunks filled since the server started: " + RandomChunks.filledChunks()), false);
        source.sendSuccess(() -> Component.literal("Spawn protection radius: " + config.spawnProtectionRadius + " chunks"), false);
        return pool.size();
    }

    private static int startPregen(CommandContext<CommandSourceStack> context, ColumnPos center) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = DimensionArgument.getDimension(context, "dimension");
        int radius = IntegerArgumentType.getInteger(context, "radius");

        if (PregenTask.isRunning()) {
            source.sendFailure(Component.literal("A pregeneration is already running. Use /randomchunks pregen stop first."));
            return 0;
        }

        ChunkPos centerChunk;
        if (center != null) {
            centerChunk = center.toChunkPos();
        } else {
            // Around the world spawn when it is in that dimension, around 0,0 otherwise.
            LevelData.RespawnData spawn = level.getRespawnData();
            centerChunk = spawn != null && spawn.dimension().equals(level.dimension())
                    ? ChunkPos.containing(spawn.pos())
                    : ChunkPos.ZERO;
        }

        PregenTask task = PregenTask.start(level, centerChunk, radius);
        if (task == null) {
            source.sendFailure(Component.literal("A pregeneration is already running."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[Random Chunks] Pregeneration started in " + task.dimensionName()
                + ", radius " + radius + " around chunk " + centerChunk.x() + ", " + centerChunk.z()
                + " (" + task.total() + " chunks).").withStyle(ChatFormatting.GREEN), true);
        return task.total();
    }

    private static int stopPregen(CommandSourceStack source) {
        PregenTask task = PregenTask.stop();
        if (task == null) {
            source.sendFailure(Component.literal("No pregeneration is running."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[Random Chunks] Pregeneration cancelled (" + task.done() + "/"
                + task.total() + " chunks generated).").withStyle(ChatFormatting.GREEN), true);
        RandomChunks.LOGGER.info("[Pregen] {} cancelled at {}/{}.", task.dimensionName(), task.done(), task.total());
        return task.done();
    }

    private static int pregenStatus(CommandSourceStack source) {
        PregenTask task = PregenTask.active();
        if (task == null) {
            source.sendSuccess(() -> Component.literal("[Random Chunks] No pregeneration is running.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[Random Chunks] Pregeneration of " + task.dimensionName() + ": "
                + task.done() + "/" + task.total() + " (" + task.percent() + "%), "
                + PregenTask.formatTime(task.elapsedSeconds()) + " elapsed").withStyle(ChatFormatting.AQUA), false);
        return task.percent();
    }
}
