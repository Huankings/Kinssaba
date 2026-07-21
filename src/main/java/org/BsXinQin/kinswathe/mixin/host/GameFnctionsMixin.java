package org.BsXinQin.kinswathe.mixin.host;

import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.BsXinQin.kinswathe.roles.cook.CookComponent;
import org.BsXinQin.kinswathe.roles.hunter.HunterComponent;
import org.BsXinQin.kinswathe.roles.kidnapper.KidnapperComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameFunctions.class)
public class GameFnctionsMixin {

    @Inject(method = "resetPlayer", at = @At("TAIL"))
    private static void resetPlayer(@NotNull ServerPlayerEntity player, CallbackInfo ci) {
        GameSafeComponent.KEY.get(player.getWorld()).reset();
        PlayerEffectComponent.KEY.get(player).reset();
        AbilityPlayerComponent.KEY.get(player).reset();
        CookComponent.KEY.get(player).reset();
        HunterComponent.KEY.get(player).reset();
        KidnapperComponent.KEY.get(player).reset();
        PhysicianComponent.KEY.get(player).reset();
        TechnicianComponent.KEY.get(player).reset();
    }

    @Inject(method = "initializeGame", at = @At("RETURN"))
    private static void autoJoinVoiceChat(@NotNull ServerWorld serverWorld, CallbackInfo ci) {
        if (!KinsWatheConfig.HANDLER.instance().EnableAutoJoinVoiceChat) return;
        serverWorld.getServer().execute(() -> {
            MapVariablesWorldComponent areas = MapVariablesWorldComponent.KEY.get(serverWorld);
            List<ServerPlayerEntity> readyPlayers = serverWorld.getPlayers(player -> areas.getReadyArea().contains(player.getPos()));
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (player == null) return;
                if (player.isSpectator()) {
                    TrainVoicePlugin.addPlayer(player.getUuid());
                } else if (readyPlayers.contains(player)) {
                    TrainVoicePlugin.resetPlayer(player.getUuid());
                }
            }
        });
    }
}
