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

    public static boolean countsAsHackerFilteredKillerCohort(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        Role role = gameWorld.getRole(player);
        /*
         * 这个 helper 只服务 Hacker 破解 HUD 的目标过滤：
         * Hacker 不应该提示破解真杀手、Hacker、Dreamer、Noelles 杀手侧中立和 Mimic。
         * 不要把它注册进 registerCohortState，否则会重新制造中立角色反查杀手的双向泄露。
         */
        return role != null
                && (gameWorld.canUseKillerFeatures(player)
                || KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role)
                || KinsWatheRoles.isKillerSidedNeutral(player));
    }

    public static boolean countsAsTwoWayKillerCohort(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        Role role = gameWorld.getRole(player);
        /*
         * 双向 cohort 只放“自己也应该有查看同伙提示资格”的角色。
         * 真杀手由 Wathe 原始 vanillaValue 处理；这里额外放行 Hacker 和 Noelles Executioner。
         */
        return role != null
                && (role == KinsWatheRoles.HACKER
                || isNoellesExecutioner(gameWorld, player));
    }

    public static boolean showsAsOneWayKillerCohortTarget(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        Role role = gameWorld.getRole(player);
        if (role == null) {
            return false;
        }

        /*
         * Dreamer、Jester、Vulture、Mimic 等只作为“目标”显示给杀手侧玩家。
         * Hacker 与 Executioner 已经在双向规则处理，所以从单向目标里排除。
         */
        boolean kinsOneWayKillerNeutral = KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role)
                && role != KinsWatheRoles.HACKER;
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
