package fr.lorderis.randomchunks.mixin;

import fr.lorderis.randomchunks.RandomChunks;
import fr.lorderis.randomchunks.pregen.PregenTask;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    private int emptyTicks;

    /** Block tags are bound by now and no chunk has been generated yet: build the block pool. */
    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void randomchunks$onLoadLevel(CallbackInfo ci) {
        RandomChunks.onServerStarting();
    }

    /**
     * Drives the pregeneration. Injected at the head because a dedicated server with nobody online
     * pauses by returning early from this method, and a pregen is typically left running on an
     * empty server; the pause is held off for as long as it runs, so chunks keep unloading.
     */
    @Inject(method = "tickServer", at = @At("HEAD"))
    private void randomchunks$tickPregen(BooleanSupplier haveTime, CallbackInfo ci) {
        if (PregenTask.isRunning()) {
            this.emptyTicks = 0;
            PregenTask.tickActive();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void randomchunks$onStop(CallbackInfo ci) {
        PregenTask.abandon();
    }
}
