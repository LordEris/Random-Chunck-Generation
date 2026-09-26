package fr.lorderis.randomchunks.pregen;

import fr.lorderis.randomchunks.RandomChunks;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

/**
 * Generates a square of chunks ahead of time, ring by ring from its centre, so exploring it later
 * costs nothing.
 *
 * <p>Chunks are requested asynchronously through loading tickets and handed back as soon as they
 * are generated: the server keeps ticking normally, and only a bounded number of chunks is ever
 * held in memory. Everything here runs on the server thread.
 */
public final class PregenTask {
    /**
     * Loads a chunk all the way to FULL without making it tick, never expires on its own and is
     * never saved, so an interrupted pregen leaves nothing behind.
     */
    private static final TicketType TICKET = TicketType.PLAYER_LOADING;
    private static final int PROGRESS_EVERY = 100;

    private static PregenTask active;

    private final ServerLevel level;
    private final long[] chunks;
    private final int chunksPerTick;
    private final int maxInFlight;
    private final ArrayDeque<Request> inFlight = new ArrayDeque<>();
    private final long startNanos = System.nanoTime();
    private int next;
    private int done;
    private int lastReported;

    private record Request(ChunkPos pos, CompletableFuture<?> future) {
    }

    private PregenTask(ServerLevel level, ChunkPos center, int radius, int chunksPerTick) {
        this.level = level;
        this.chunksPerTick = chunksPerTick;
        // Enough requests queued to keep every world-gen thread busy, few enough that the chunks
        // waiting to be unloaded stay a handful.
        this.maxInFlight = Math.max(16, chunksPerTick * 4);

        int side = 2 * radius + 1;
        this.chunks = new long[side * side];
        int i = 0;
        for (int ring = 0; ring <= radius; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.abs(dx) == ring || Math.abs(dz) == ring) {
                        this.chunks[i++] = ChunkPos.pack(center.x() + dx, center.z() + dz);
                    }
                }
            }
        }
    }

    public static PregenTask active() {
        return active;
    }

    public static boolean isRunning() {
        return active != null;
    }

    /** Returns the new task, or {@code null} if one is already running. */
    public static PregenTask start(ServerLevel level, ChunkPos center, int radius) {
        if (active != null) {
            return null;
        }
        PregenTask task = new PregenTask(level, center, radius, RandomChunks.config().pregenChunksPerTick);
        active = task;
        RandomChunks.LOGGER.info("[Pregen] Starting in {} - {} chunks to generate ({} per tick).",
                task.dimensionName(), task.total(), task.chunksPerTick);
        return task;
    }

    /** Cancels the running task, if any, and returns it. */
    public static PregenTask stop() {
        PregenTask task = active;
        if (task != null) {
            task.releaseTickets();
            active = null;
        }
        return task;
    }

    /** Forgets the running task without touching the chunk system, which is shutting down. */
    public static void abandon() {
        active = null;
    }

    public static void tickActive() {
        PregenTask task = active;
        if (task != null && task.tick()) {
            active = null;
        }
    }

    /** Returns true once every chunk has been generated. */
    private boolean tick() {
        ServerChunkCache chunkSource = this.level.getChunkSource();

        Iterator<Request> requests = this.inFlight.iterator();
        while (requests.hasNext()) {
            Request request = requests.next();
            if (request.future().isDone()) {
                // The chunk is generated and saved with the level: let it unload like any other.
                chunkSource.removeTicketWithRadius(TICKET, request.pos(), 0);
                requests.remove();
                this.done++;
            }
        }

        for (int sent = 0; sent < this.chunksPerTick && this.inFlight.size() < this.maxInFlight
                && this.next < this.chunks.length; sent++) {
            ChunkPos pos = ChunkPos.unpack(this.chunks[this.next++]);
            this.inFlight.add(new Request(pos, chunkSource.addTicketAndLoadWithRadius(TICKET, pos, 0)));
        }

        if (this.done - this.lastReported >= PROGRESS_EVERY) {
            this.lastReported = this.done;
            long elapsed = this.elapsedSeconds();
            long eta = this.done == 0 ? 0 : elapsed * (this.total() - this.done) / this.done;
            RandomChunks.LOGGER.info("[Pregen] {}: {}/{} ({}%) - {} elapsed - ETA {}",
                    this.dimensionName(), this.done, this.total(), this.percent(), formatTime(elapsed), formatTime(eta));
        }

        if (this.done >= this.chunks.length) {
            RandomChunks.LOGGER.info("[Pregen] {} finished in {} - {} chunks generated.",
                    this.dimensionName(), formatTime(this.elapsedSeconds()), this.total());
            return true;
        }
        return false;
    }

    private void releaseTickets() {
        ServerChunkCache chunkSource = this.level.getChunkSource();
        for (Request request : this.inFlight) {
            chunkSource.removeTicketWithRadius(TICKET, request.pos(), 0);
        }
        this.inFlight.clear();
    }

    public int done() {
        return this.done;
    }

    public int total() {
        return this.chunks.length;
    }

    public int percent() {
        return (int) (this.done * 100L / this.chunks.length);
    }

    public String dimensionName() {
        return this.level.dimension().identifier().toString();
    }

    public long elapsedSeconds() {
        return (System.nanoTime() - this.startNanos) / 1_000_000_000L;
    }

    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            return (seconds / 60) + "m" + (seconds % 60) + "s";
        }
        return (seconds / 3600) + "h" + ((seconds % 3600) / 60) + "m";
    }
}
