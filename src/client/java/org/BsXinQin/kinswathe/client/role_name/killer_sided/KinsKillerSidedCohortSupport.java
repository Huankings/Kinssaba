package org.BsXinQin.kinswathe.client.role_name.killer_sided;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.jetbrains.annotations.NotNull;

/**
 * kinssaba 自己的杀手侧同伙目标判定工具。
 */
public final class KinsKillerSidedCohortSupport {
    private KinsKillerSidedCohortSupport() {
    }

    public static boolean showsAsOneWayKillerCohortTarget(@NotNull GameWorldComponent gameWorld, @NotNull PlayerEntity player) {
        Role role = gameWorld.getRole(player);
        if (role == null) {
            return false;
        }

        boolean kinsOneWayKillerNeutral = KinsWatheRoles.KILLER_NEUTRAL_ROLES.contains(role);
        return kinsOneWayKillerNeutral;
    }
}
