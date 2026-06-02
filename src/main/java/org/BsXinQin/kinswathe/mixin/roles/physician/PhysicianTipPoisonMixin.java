package org.BsXinQin.kinswathe.mixin.roles.physician;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.UUID;

@Mixin(PlayerPoisonComponent.class)
public abstract class PhysicianTipPoisonMixin {

    @Shadow @Final @NotNull private PlayerEntity player;

    @Inject(method = "setDetailedPoisonTicks", at = @At("HEAD"))
    private void tipPhysicianPoison(int ticks, @NotNull UUID poisoner, @NotNull Identifier source, @NotNull NbtCompound extra, CallbackInfo ci) {
        if (ticks <= 0 || GameFunctions.isPlayerSpectatingOrCreative(this.player)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.getWorld());
        if (gameWorld.isRole(this.player, KinsWatheRoles.ROBOT) || !(this.player instanceof ServerPlayerEntity) || (poisoner != null && FabricLoader.getInstance().isModLoaded("noellesroles") && gameWorld.isRole(poisoner, KinsWatheRoles.noellesrolesRoles("BARTENDER")))) return;
        for (ServerPlayerEntity serverPlayer : this.player.getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayer == null) continue;
            if (gameWorld.isRole(serverPlayer, KinsWatheRoles.PHYSICIAN) && GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
                serverPlayer.sendMessage(Text.translatable("tip.kinswathe.physician.poisoned").withColor(Color.RED.getRGB()), true);
            }
        }
    }
}
