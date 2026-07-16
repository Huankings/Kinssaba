package org.BsXinQin.kinswathe.client.instinct.roles.physician;

import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.agmas.noellesroles.framing.DelusionPlayerComponent;

import java.awt.Color;

public final class PhysicianInstinctHandler {
    private PhysicianInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/physician_marks"), KinsWatheInstinctHandlers.PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer) || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return InstinctApi.HighlightResult.pass();
            }

            PlayerPoisonComponent targetPoison = PlayerPoisonComponent.KEY.get(targetPlayer);
            DelusionPlayerComponent targetDelusion = DelusionPlayerComponent.KEY.get(targetPlayer);
            if (GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.PHYSICIAN)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && (targetPoison.poisonTicks > 0 || targetDelusion.isActive())) {
                /*
                 * 医师看到中毒/幻觉目标是治疗职业的能力提示。
                 * 它不依赖本能键，所以保持为独立 ability mark。
                 */
                return InstinctApi.HighlightResult.color(Color.RED.getRGB());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
