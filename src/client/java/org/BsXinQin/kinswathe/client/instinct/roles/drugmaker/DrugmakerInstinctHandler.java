package org.BsXinQin.kinswathe.client.instinct.roles.drugmaker;

import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;

import java.util.UUID;

public final class DrugmakerInstinctHandler {
    private static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000");

    private DrugmakerInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/drugmaker_marks"), KinsWatheInstinctHandlers.PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer) || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return InstinctApi.HighlightResult.pass();
            }

            PlayerPoisonComponent targetPoison = PlayerPoisonComponent.KEY.get(targetPlayer);
            if (GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.DRUGMAKER)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && !WatheClient.isInstinctEnabled()
                    && targetPoison.poisonTicks > 0
                    && !(targetPoison.poisoner != null && targetPoison.poisoner.equals(DELUSION_MARKER))) {
                /*
                 * 制毒师提示只在未开启本能时显示，避免和杀手/其它本能颜色抢优先级。
                 * 幻觉试剂旧 marker 明确排除，避免把幻觉误判成制毒师投毒。
                 */
                return InstinctApi.HighlightResult.color(KinsWatheRoles.DRUGMAKER.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
