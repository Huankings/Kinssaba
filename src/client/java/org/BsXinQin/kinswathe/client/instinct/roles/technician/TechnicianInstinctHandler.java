package org.BsXinQin.kinswathe.client.instinct.roles.technician;

import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.BsXinQin.kinswathe.roles.technician.TechnicianComponent;

public final class TechnicianInstinctHandler {
    private TechnicianInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/technician_marks"), KinsWatheInstinctHandlers.PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.TECHNICIAN)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && TechnicianComponent.KEY.get(targetPlayer).technicianTicks > 0) {
                /*
                 * 技术员捕获/标记目标显示职业色，是技能状态提示，不依赖本能键。
                 */
                return InstinctApi.HighlightResult.color(KinsWatheRoles.TECHNICIAN.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
