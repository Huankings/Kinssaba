package org.BsXinQin.kinswathe.client.instinct.roles.cook;

import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.BsXinQin.kinswathe.roles.cook.CookComponent;

import java.awt.Color;

public final class CookInstinctHandler {
    private CookInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/cook_marks"), KinsWatheInstinctHandlers.PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.COOK)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && CookComponent.KEY.get(targetPlayer).eatTicks > 0) {
                /*
                 * 厨师看到“正在被吃/被标记”的目标是职业能力反馈，不需要本能键开启。
                 */
                return InstinctApi.HighlightResult.color(Color.GREEN.getRGB());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
