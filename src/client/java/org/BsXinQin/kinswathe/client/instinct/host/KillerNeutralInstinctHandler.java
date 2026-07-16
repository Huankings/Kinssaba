package org.BsXinQin.kinswathe.client.instinct.host;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;

public final class KillerNeutralInstinctHandler {
    private KillerNeutralInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/killer_neutral_colors"), KinsWatheInstinctHandlers.PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer)
                    || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    || !WatheClient.isInstinctEnabled()
                    || !WatheClient.isKiller()
                    || !WatheClient.isPlayerAliveAndInSurvival()) {
                return InstinctApi.HighlightResult.pass();
            }

            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(viewer.getWorld());
            Role role = gameWorld.getRole(targetPlayer);
            if (role == null) {
                return InstinctApi.HighlightResult.pass();
            }

            /*
             * KinsWathe 对杀手看中立有额外颜色规则：
             * 普通中立显示接近平民的绿色，杀手侧中立显示自己的职业色。
             * 这仍是杀手本能的一部分，所以必须依赖 WatheClient.isInstinctEnabled()。
             */
            if (KinsWatheRoles.NEUTRAL_ROLES.contains(role)) {
                return InstinctApi.HighlightResult.color(0x4EDD35);
            }
            if (KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role)) {
                return InstinctApi.HighlightResult.color(role.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
