package org.BsXinQin.kinswathe.client.role_name.killer_sided;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;

/**
 * kinssaba 与 NoellesRoles 共享的杀手侧同伙判定工具。
 *
 * <p>这里刻意拆出“双向同伙资格”和“单向目标显示”两套判断：
 * 双向资格会让 viewer 自己获得识别同伙能力；单向目标只让某些中立在杀手侧眼里显示为同伙。</p>
 */
public final class KinsKillerSidedCohortSupport {
    private KinsKillerSidedCohortSupport() {
    }

    public static boolean countsAsTwoWayKillerCohort(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        Role role = gameWorld.getRole(player);
        /*
         * 双向 cohort 只放“自己也应该有查看同伙提示资格”的角色。
         * 真杀手由 Wathe 原始 vanillaValue 处理；这里额外放行 Noelles Executioner。
         */
        return role != null
                && isNoellesExecutioner(gameWorld, player);
    }

    public static boolean showsAsOneWayKillerCohortTarget(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        Role role = gameWorld.getRole(player);
        if (role == null) {
            return false;
        }

        /*
         * Jester、Vulture、Mimic 等只作为“目标”显示给杀手侧玩家。
         * Executioner 已经在双向规则处理，所以从单向目标里排除。
         */
        boolean kinsOneWayKillerNeutral = KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role);
        boolean noellesOneWayKillerSided = KinsWatheRoles.isKillerSidedNeutral(player)
                && !isNoellesExecutioner(gameWorld, player);
        return kinsOneWayKillerNeutral || noellesOneWayKillerSided;
    }

    private static boolean isNoellesExecutioner(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        if (!FabricLoader.getInstance().isModLoaded("noellesroles")) {
            return false;
        }

        Role executioner = KinsWatheRoles.noellesrolesRoles("EXECUTIONER");
        return executioner != null && gameWorld.isRole(player, executioner);
    }
}
