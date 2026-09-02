package org.BsXinQin.kinswathe.client.role_name.killer_sided;

import dev.doctor4t.wathe.api.client.gui.RoleNameHudApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import org.BsXinQin.kinswathe.KinsWathe;

/**
 * kinssaba 杀手侧同伙文字接入 Wathe RoleName HUD API 的规则。
 *
 * <p>这个类替代旧 KillerSidedTextMixin / KillerCohortHudMixin，
 * 并把双向同伙资格与单向目标显示拆成两个 API 注册点。</p>
 */
public final class KillerSidedCohortHudHandler {
    private KillerSidedCohortHudHandler() {
    }

    public static void register() {
        RoleNameHudApi.registerCohortTargetState(
                KinsWathe.id("role_name/one_way_killer_sided_targets"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                (viewer, target, vanillaValue) -> {
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(target.getWorld());
                    /*
                     * 单向目标状态只保留 kinssaba 自己的中立职业。
                     */
                    return KinsKillerSidedCohortSupport.showsAsOneWayKillerCohortTarget(gameWorld, target)
                            ? true
                            : null;
                }
        );
    }
}
