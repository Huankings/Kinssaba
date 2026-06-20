package org.BsXinQin.kinswathe.client.mixin.roles.hacker;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(WatheClient.class)
public abstract class HackerInstinctMixin {

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void isInstinctEnabled(@NotNull CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance().player == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        /*
         * 黑客的本能由“本能键输入是否激活”控制。
         * 统一入口会读取玩家自己的 /instinct key 设置，避免这里硬编码成长按模式。
         */
        if (gameWorld.isRole(MinecraftClient.getInstance().player, KinsWatheRoles.HACKER) && WatheClient.isInstinctInputActive()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlight(@NotNull Entity target, @NotNull CallbackInfoReturnable<Integer> cir) {
        if (MinecraftClient.getInstance().player == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        if (target instanceof @NotNull PlayerEntity targetPlayer) {
            if (GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                HackerComponent targetHack = HackerComponent.KEY.get(targetPlayer);
                if (gameWorld.isRole(MinecraftClient.getInstance().player, KinsWatheRoles.HACKER) && WatheClient.isInstinctEnabled()) {
                    if (gameWorld.getRole(targetPlayer) != null) {
                        if (gameWorld.canUseKillerFeatures(targetPlayer) || (FabricLoader.getInstance().isModLoaded("noellesroles") && gameWorld.isRole(targetPlayer, KinsWatheRoles.noellesrolesRoles("MIMIC")))) {
                            cir.setReturnValue(MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F));
                        } else if (KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(gameWorld.getRole(targetPlayer)) || KinsWatheRoles.isKillerSidedNeutral(targetPlayer)) {
                            cir.setReturnValue(gameWorld.getRole(targetPlayer).color());
                        } else if (targetHack.hackingTime >= ConfigWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld()).HackerHackingTime * 20) {
                            cir.setReturnValue(Color.GREEN.getRGB());
                        } else {
                            cir.setReturnValue(KinsWatheRoles.HACKER.color());
                        }
                    }
                }
            }
        }
    }
}
