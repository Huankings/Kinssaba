package org.BsXinQin.kinswathe.host.movement;

/**
 * kinssaba 的玩家移动 API 接入入口。
 */
public final class KinsWatheMovementBootstrap {
    private KinsWatheMovementBootstrap() {
    }

    public static void init() {
        PlayerEffectMovementHandler.init();
    }
}
