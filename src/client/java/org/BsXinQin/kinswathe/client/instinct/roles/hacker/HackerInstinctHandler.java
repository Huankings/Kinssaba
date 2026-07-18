package org.BsXinQin.kinswathe.client.instinct.roles.hacker;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;

import java.awt.Color;

public final class HackerInstinctHandler {
    private HackerInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerAvailability(KinsWathe.id("instinct/hacker_availability"), InstinctApi.DEFAULT_PRIORITY, viewer -> {
            if (GameFunctions.isPlayerAliveAndSurvival(viewer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.HACKER)
                    && WatheClient.isInstinctInputActive()) {
                /*
                 * 黑客本能只在黑客仍存活时开启。
                 * 死亡后如果继续返回 ENABLE，会把观察者本能误导成黑客自己的高优先级颜色链路。
                 */
                return InstinctApi.AvailabilityResult.ENABLE;
            }
            return InstinctApi.AvailabilityResult.PASS;
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/hacker_targets"), KinsWatheInstinctHandlers.PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer)
                    || !GameFunctions.isPlayerAliveAndSurvival(viewer)
                    || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    || !GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.HACKER)
                    || !WatheClient.isInstinctEnabled()) {
                return InstinctApi.HighlightResult.pass();
            }

            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(viewer.getWorld());
            Role targetRole = gameWorld.getRole(targetPlayer);
            if (targetRole == null) {
                return InstinctApi.HighlightResult.pass();
            }

            HackerComponent targetHack = HackerComponent.KEY.get(targetPlayer);
            Role mimic = FabricLoader.getInstance().isModLoaded("noellesroles") ? KinsWatheRoles.noellesrolesRoles("MIMIC") : null;
            if (gameWorld.canUseKillerFeatures(targetPlayer) || (mimic != null && gameWorld.isRole(targetPlayer, mimic))) {
                /*
                 * 黑客本能会把杀手和 Mimic 视为红色危险目标。
                 * 这条信息只应该给存活黑客看，死亡后统一改走观察者职业色。
                 */
                return InstinctApi.HighlightResult.color(MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F));
            }
            if (KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(targetRole) || KinsWatheRoles.isKillerSidedNeutral(targetPlayer)) {
                return InstinctApi.HighlightResult.color(targetRole.color());
            }
            if (targetHack.hackingTime >= ConfigWorldComponent.KEY.get(viewer.getWorld()).HackerHackingTime * 20) {
                return InstinctApi.HighlightResult.color(Color.GREEN.getRGB());
            }
            return InstinctApi.HighlightResult.color(KinsWatheRoles.HACKER.color());
        });
    }
}
