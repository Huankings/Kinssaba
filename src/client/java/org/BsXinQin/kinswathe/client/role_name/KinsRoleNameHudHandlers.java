package org.BsXinQin.kinswathe.client.role_name;

import org.BsXinQin.kinswathe.client.role_name.killer_sided.KillerSidedCohortHudHandler;
import org.BsXinQin.kinswathe.client.role_name.roles.detective.DetectiveTargetHudHandler;
import org.BsXinQin.kinswathe.client.role_name.roles.physician.PhysicianBodyHudHandler;

/**
 * kinssaba 接入 Wathe RoleName HUD API 的总入口。
 *
 * <p>这里只保留各职业/词条的注册顺序。具体逻辑已经拆到独立 handler：
 * 杀手侧同伙规则、Detective 目标 HUD、Physician 尸体 HUD。</p>
 */
public final class KinsRoleNameHudHandlers {
    private KinsRoleNameHudHandlers() {
    }

    public static void register() {
        KillerSidedCohortHudHandler.register();
        DetectiveTargetHudHandler.register();
        PhysicianBodyHudHandler.register();
    }
}
