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
        RoleNameHudApi.registerCohortState(
                KinsWathe.id("role_name/killer_sided_cohorts"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                (viewer, subject, vanillaValue) -> {
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(subject.getWorld());
                    /*
                     * 只有 Hacker 与 Noelles Executioner 在这里返回 true。
                     * Dreamer、Jester、Vulture、Mimic 等只应作为目标被显示，不能让它们获得反查资格。
                     */
                    return KinsKillerSidedCohortSupport.countsAsTwoWayKillerCohort(gameWorld, subject)
                            ? true
                            : null;
                }
        );

        RoleNameHudApi.registerCohortTargetState(
                KinsWathe.id("role_name/one_way_killer_sided_targets"),
                RoleNameHudApi.DEFAULT_PRIORITY,
                (viewer, target, vanillaValue) -> {
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(target.getWorld());
                    /*
                     * 单向目标状态只决定“target 看起来是不是杀手同伙”，不赋予 viewer 任何新能力。
                     * Wathe 最终仍会检查 viewer 自己是否有同伙识别资格，再决定是否真的绘制提示。
                     */
                    return KinsKillerSidedCohortSupport.showsAsOneWayKillerCohortTarget(gameWorld, target)
                            ? true
                            : null;
                }
        );
    }
}
