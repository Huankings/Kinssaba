package org.BsXinQin.kinswathe.host.death;

import dev.doctor4t.wathe.api.death.DeathApi;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.KinsWatheConfig;

/**
 * kinssaba 对 Wathe 击杀金币收益的配置化补差。
 */
public final class KillRewardHandler {
    private static boolean initialized = false;

    private KillRewardHandler() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        DeathApi.registerAfterAttempt(
                KinsWathe.id("configured_kill_reward_delta"),
                DeathApi.PRIORITY_POST_CONFIRMED_DEATH,
                context -> {
                    if (!KinsWatheConfig.HANDLER.instance().EnableWatheModify
                            || !context.confirmedDeath()
                            || context.killer() == null) {
                        return;
                    }

                    PlayerEntity killer = context.killer();
                    GameWorldComponent gameWorld = GameWorldComponent.KEY.get(killer.getWorld());
                    if (gameWorld.isInnocent(killer)) {
                        return;
                    }

                    /*
                     * 旧 mixin 在 killPlayer HEAD 额外发放：
                     * IncreaseMoneyWhenKill - 100。
                     *
                     * 这里仍保留“只补差额”的语义，让 Wathe 自己的默认杀手击杀收益继续负责基础 100 金。
                     * 但执行点改到 DeathApi 的死亡确认后，护盾、免死、双重人格致死转化等取消死亡的场景
                     * 不会再误发金币。
                     */
                    int rewardDelta = KinsWatheConfig.HANDLER.instance().IncreaseMoneyWhenKill - GameConstants.MONEY_PER_KILL;
                    if (rewardDelta != 0) {
                        PlayerShopComponent.KEY.get(killer).addToBalance(rewardDelta);
                    }
                }
        );
    }
}
