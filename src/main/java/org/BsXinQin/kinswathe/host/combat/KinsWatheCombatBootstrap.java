package org.BsXinQin.kinswathe.host.combat;

/**
 * kinssaba 对 Wathe 枪械流程的公开 API 接入总入口。
 */
public final class KinsWatheCombatBootstrap {
    private static boolean initialized = false;

    private KinsWatheCombatBootstrap() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        KillerRevolverPenaltyHandler.init();
    }
}
