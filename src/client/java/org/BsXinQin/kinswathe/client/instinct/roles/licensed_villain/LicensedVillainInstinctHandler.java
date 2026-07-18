package org.BsXinQin.kinswathe.client.instinct.roles.licensed_villain;

import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;

public final class LicensedVillainInstinctHandler {
    private LicensedVillainInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerAvailability(KinsWathe.id("instinct/licensed_villain_availability"), InstinctApi.DEFAULT_PRIORITY, viewer -> {
            if (GameFunctions.isPlayerAliveAndSurvival(viewer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.LICENSED_VILLAIN)
                    && WatheClient.isInstinctInputActive()) {
                /*
                 * 执照恶棍本能只在本人仍存活时开启。
                 * 死亡观察者不应因为保留职业身份而继续获得这条本能资格。
                 */
                return InstinctApi.AvailabilityResult.ENABLE;
            }
            return InstinctApi.AvailabilityResult.PASS;
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/licensed_villain_targets"), KinsWatheInstinctHandlers.PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(viewer)
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.LICENSED_VILLAIN)
                    && WatheClient.isInstinctEnabled()) {
                /*
                 * 执照恶棍是按本能键开启的独立本能职业，所有存活玩家显示为职业色。
                 * viewer 死亡后不再返回该职业色，避免盖掉 Harpy 观察者职业色。
                 */
                return InstinctApi.HighlightResult.color(KinsWatheRoles.LICENSED_VILLAIN.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
