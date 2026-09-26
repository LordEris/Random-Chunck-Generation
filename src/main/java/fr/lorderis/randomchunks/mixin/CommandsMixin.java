package fr.lorderis.randomchunks.mixin;

import com.mojang.brigadier.CommandDispatcher;
import fr.lorderis.randomchunks.command.RandomChunksCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers {@code /randomchunks} alongside the vanilla commands, without needing Fabric API. */
@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void randomchunks$registerCommand(Commands.CommandSelection selection, CommandBuildContext context,
                                              CallbackInfo ci) {
        RandomChunksCommand.register(this.dispatcher);
    }
}
