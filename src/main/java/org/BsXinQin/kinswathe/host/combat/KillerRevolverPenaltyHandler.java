package org.BsXinQin.kinswathe.host.combat;

import dev.doctor4t.wathe.api.combat.GunShotApi;
import dev.doctor4t.wathe.api.combat.RevolverPenaltyResult;
import dev.doctor4t.wathe.game.GameFunctions;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;

/**
 * kinssaba 配置项：杀手左轮击中无辜者后不掉枪。
 */
public final class KillerRevolverPenaltyHandler {
    private static boolean initialized = false;

    private KillerRevolverPenaltyHandler() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        GunShotApi.registerInnocentRevolverPenaltyRule(
                KinsWathe.id("killer_revolver_penalty"),
                GunShotApi.DEFAULT_PRIORITY,
                context -> {
                    if (!KinsWatheConfig.HANDLER.instance().EnableWatheModify
                            || !KinsWatheConfig.HANDLER.instance().PreventKillerDropRevolver) {
                        return RevolverPenaltyResult.PASS;
                    }

                    /*
                     * 旧 mixin 取消的是 Wathe 延迟掉枪 lambda。
                     * GunShotApi 的 SKIP 会跳过整段“误伤好人惩罚”，其中包含掉枪和清空心情。
                     * 对杀手来说原版不会触发自我反火，所以这里等价于“阻止杀手误杀好人后掉左轮”。
                     */
                    return context.game().canUseKillerFeatures(context.shooter())
                            && GameFunctions.isPlayerAliveAndSurvival(context.shooter())
                            ? RevolverPenaltyResult.SKIP
                            : RevolverPenaltyResult.PASS;
                }
        );
    }
}
