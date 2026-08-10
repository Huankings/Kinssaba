package org.BsXinQin.kinswathe.host.movement;

import dev.doctor4t.wathe.api.movement.PlayerMovementApi;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;

/**
 * 玩家效果对移动速度的影响。
 */
public final class PlayerEffectMovementHandler {
    private static final int PRIORITY = 5000;

    private PlayerEffectMovementHandler() {
    }

    public static void init() {
        PlayerMovementApi.registerSpeedModifier(KinsWathe.id("movement/stun"), PRIORITY, context -> {
            PlayerEntity player = context.player();
            if (GameFunctions.isPlayerAliveAndSurvival(player)
                    && PlayerEffectComponent.KEY.get(player).stunTicks > 0) {
                /*
                 * 原先这里通过 mixin 把 PlayerEntity#getMovementSpeed 直接改成 0。
                 * 现在接入 Wathe MovementApi，服务端和客户端都会经过同一条公开速度链路。
                 */
                return PlayerMovementApi.MovementSpeedResult.override(0.0F);
            }
            return PlayerMovementApi.MovementSpeedResult.pass();
        });
    }
}
