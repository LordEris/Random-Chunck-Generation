package fr.lorderis.randomchunks.mixin;

import fr.lorderis.randomchunks.gen.ChunkFiller;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Fills each chunk with its random block right before its light is computed.
 *
 * <p>The {@code LIGHT} step requires every neighbour to be at {@code INITIALIZE_LIGHT}, which comes
 * after {@code FEATURES}: all decoration that could write into this chunk has happened, and the
 * light and final heightmaps are then computed on the finished chunk.
 */
@Mixin(ChunkStatusTasks.class)
public abstract class ChunkStatusTasksMixin {
    @Inject(method = "light", at = @At("HEAD"))
    private static void randomchunks$fillBeforeLight(WorldGenContext context, ChunkStep step,
                                                     StaticCache2D<GenerationChunkHolder> chunks, ChunkAccess chunk,
                                                     CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        ChunkFiller.onLightStep(context.level(), context.generator(), chunk);
    }
}
