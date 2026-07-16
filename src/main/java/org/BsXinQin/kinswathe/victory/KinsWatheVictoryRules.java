package org.BsXinQin.kinswathe.victory;

import dev.doctor4t.wathe.api.win.CustomVictory;
import dev.doctor4t.wathe.api.win.VictoryApi;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheRoles;

import java.util.List;

/**
 * kinssaba 接入 Wathe 公开胜利 API 的集中入口。
 *
 * <p>执照恶棍以前通过 LicensedVillainKeepGameMixin 卡在 MurderGameMode 的局部 winStatus 上。
 * 现在 Wathe 会在同一位置主动询问 VictoryApi，因此这里直接表达它的规则：
 * 活着时阻止普通杀手 / 乘客结束，只剩自己时触发独立胜利。</p>
 */
public final class KinsWatheVictoryRules {
    private KinsWatheVictoryRules() {
    }

    public static void init() {
        VictoryApi.registerRule(KinsWathe.id("victory/licensed_villain"), VictoryApi.DEFAULT_PRIORITY, context -> {
            List<ServerPlayerEntity> alivePlayers = context.alivePlayers();
            List<ServerPlayerEntity> licensedVillains = alivePlayers.stream()
                    .filter(player -> context.gameWorld().isRole(player, KinsWatheRoles.LICENSED_VILLAIN))
                    .toList();

            if (licensedVillains.isEmpty()) {
                return VictoryApi.VictoryResult.pass();
            }

            if (alivePlayers.size() == 1) {
                return VictoryApi.VictoryResult.customWin(
                        CustomVictory.of(
                                KinsWatheRoles.LICENSED_VILLAIN.identifier(),
                                KinsWatheRoles.LICENSED_VILLAIN.color(),
                                licensedVillains
                        )
                );
            }

            if (context.vanillaWinStatus() == GameFunctions.WinStatus.KILLERS
                    || context.vanillaWinStatus() == GameFunctions.WinStatus.PASSENGERS) {
                return VictoryApi.VictoryResult.keepRunning();
            }

            return VictoryApi.VictoryResult.pass();
        });
    }
}
