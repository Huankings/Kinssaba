package org.BsXinQin.kinswathe.host.death;

/**
 * kinssaba 对 Wathe 死亡流程的公开 API 接入总入口。
 *
 * <p>这里只有初始化编排，不放具体玩法判断。
 * 后续再迁移死亡/击杀类 mixin 时，按机制或职业拆成独立 handler，
 * 避免重新把所有死亡特判塞回一个大类里。</p>
 */
public final class KinsWatheDeathBootstrap {
    private static boolean initialized = false;

    private KinsWatheDeathBootstrap() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        BodyDeathReasonHandler.init();
        KillRewardHandler.init();
    }
}
