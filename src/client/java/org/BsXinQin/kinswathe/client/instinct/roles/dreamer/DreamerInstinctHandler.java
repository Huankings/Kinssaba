package org.BsXinQin.kinswathe.client.instinct.roles.dreamer;

import dev.doctor4t.wathe.api.instinct.InstinctApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.client.instinct.KinsWatheInstinctHandlers;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;

public final class DreamerInstinctHandler {
    private DreamerInstinctHandler() {
    }

    public static void register() {
        InstinctApi.registerAvailability(KinsWathe.id("instinct/dreamer_availability"), InstinctApi.DEFAULT_PRIORITY, viewer -> {
            if (GameFunctions.isPlayerAliveAndSurvival(viewer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.DREAMER)
                    && WatheClient.isInstinctInputActive()) {
                /*
                 * 梦者主动本能只属于仍在局内存活的梦者。
                 * 死亡后角色表不会立刻清空，所以必须在资格层挡住旧身份继续开启本能。
                 */
                return InstinctApi.AvailabilityResult.ENABLE;
            }
            return InstinctApi.AvailabilityResult.PASS;
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/dreamer_targets"), KinsWatheInstinctHandlers.PRIORITY_INSTINCT_COLOR, (viewer, target) -> {
            if (target instanceof PlayerEntity targetPlayer
                    && GameFunctions.isPlayerAliveAndSurvival(viewer)
                    && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
                    && GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, KinsWatheRoles.DREAMER)
                    && WatheClient.isInstinctEnabled()) {
                /*
                 * 梦者主动本能：按本能键后，存活玩家统一显示梦者职业色。
                 * viewer 也必须存活，避免死亡观察者的本能开启状态误触发梦者颜色。
                 * 这是标准本能链路，所以受 Convener 等 availability 禁用规则影响。
                 */
                return InstinctApi.HighlightResult.color(KinsWatheRoles.DREAMER.color());
            }
            return InstinctApi.HighlightResult.pass();
        });

        InstinctApi.registerHighlight(KinsWathe.id("instinct/dream_imprint"), KinsWatheInstinctHandlers.PRIORITY_ABILITY_MARK, (viewer, target) -> {
            if (!(target instanceof PlayerEntity targetPlayer) || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return InstinctApi.HighlightResult.pass();
            }

            DreamerComponent targetDream = DreamerComponent.KEY.get(targetPlayer);
            if (targetDream.dreamerUUID == null || targetDream.dreamArmor <= 0) {
                return InstinctApi.HighlightResult.pass();
            }

            PlayerEntity dreamer = targetPlayer.getWorld().getPlayerByUuid(targetDream.dreamerUUID);
            boolean viewerIsDreamer = viewer == dreamer && WatheClient.isPlayerAliveAndInSurvival();
            if ((viewerIsDreamer && !WatheClient.isKiller())
                    || (viewerIsDreamer && WatheClient.isKiller() && !WatheClient.isInstinctEnabled())) {
                /*
                 * 梦痕护甲是 Dreamer 放出的能力标记。
                 * 杀手身份下只有在没有开启普通本能时显示，避免和杀手本能颜色互相抢信息。
                 */
                return InstinctApi.HighlightResult.color(KinsWatheRoles.DREAMER.color());
            }
            return InstinctApi.HighlightResult.pass();
        });
    }
}
