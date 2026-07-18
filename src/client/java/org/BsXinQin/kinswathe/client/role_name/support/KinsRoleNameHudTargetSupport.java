package org.BsXinQin.kinswathe.client.role_name.support;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * kinssaba 准心目标读取工具。
 *
 * <p>Wathe 的 Context 已经把准心玩家统一算好，这里只做“必须是存活生存玩家”的职业过滤。</p>
 */
public final class KinsRoleNameHudTargetSupport {
    private KinsRoleNameHudTargetSupport() {
    }

    public static @Nullable PlayerEntity aliveTarget(@Nullable PlayerEntity target) {
        return target != null && GameFunctions.isPlayerAliveAndSurvival(target) ? target : null;
    }
}
