package org.BsXinQin.kinswathe.host.death;

import dev.doctor4t.wathe.api.death.DeathApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.component.BodyDeathReasonComponent;

/**
 * 给 Wathe 生成的新尸体写入 kinssaba 自己的死因组件。
 */
public final class BodyDeathReasonHandler {
    private static boolean initialized = false;

    private BodyDeathReasonHandler() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        DeathApi.registerBodySpawn(
                KinsWathe.id("body_death_reason"),
                DeathApi.DEFAULT_PRIORITY,
                context -> {
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(context.victim().getWorld());
                    if (gameWorld.getRole(context.victim()) == null) {
                        return;
                    }

                    /*
                     * 旧 mixin 依赖 killPlayer 内部的 body 局部变量。
                     * 现在改用 DeathApi 的尸体生成阶段：此时 body 已经写好真实死者、外观和朝向，
                     * 但还没 spawn 进世界，正适合写尸体 CCA，避免和实体同步/生成顺序打架。
                     */
                    BodyDeathReasonComponent.KEY.get(context.body()).deathReason = context.deathReason();
                }
        );
    }
}
