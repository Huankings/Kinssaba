package org.BsXinQin.kinswathe.client.instinct.noellesroles;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.loader.api.FabricLoader;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;

public final class CoronerInstinctHandler {
    private CoronerInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerHighlight(KinsWathe.id("instinct/noelles_coroner_bodies"), KinsWatheInstinctHandlers.PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!FabricLoader.getInstance().isModLoaded("noellesroles")
                    || !(target instanceof PlayerBodyEntity)
                    || !ConfigWorldComponent.KEY.get(viewer.getWorld()).EnableNoellesRolesModify
                    || !ConfigWorldComponent.KEY.get(viewer.getWorld()).CoronerInstinctModify
                    || WatheClient.moodComponent == null) {
                return InstinctApi.HighlightResult.pass();
            }

            Role coroner = KinsWatheRoles.noellesrolesRoles("CORONER");
            PlayerMoodComponent playerMood = PlayerMoodComponent.KEY.get(viewer);
            if (coroner != null
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, coroner)
                    && WatheClient.isPlayerAliveAndInSurvival()
                    && playerMood != null
                    && !playerMood.isLowerThanMid()) {
                /*
                 * NoellesRoles 验尸官联动：心情未过低时给尸体灰色提示。
                 * 这是职业能力辅助信息，不进入本能键开关链路。
                 */
                return InstinctApi.HighlightResult.color(0x606060);
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
