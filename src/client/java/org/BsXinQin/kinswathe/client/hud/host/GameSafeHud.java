package org.BsXinQin.kinswathe.client.hud.host;

import dev.doctor4t.wathe.api.client.hud.HudOverlayApi;
import dev.doctor4t.wathe.api.client.hud.HudOverlayLayer;
import net.minecraft.text.Text;
import org.BsXinQin.kinswathe.KinsWathe;
import org.BsXinQin.kinswathe.component.ConfigWorldComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;

import java.awt.Color;

/**
 * 开局安全时间顶部提示 HUD。
 */
public final class GameSafeHud {
    private GameSafeHud() {
    }

    public static void register() {
        HudOverlayApi.register(KinsWathe.id("hud/game_safe"), HudOverlayLayer.BEFORE_HUD, HudOverlayApi.DEFAULT_PRIORITY, context -> {
            ConfigWorldComponent config = ConfigWorldComponent.KEY.get(context.player().getWorld());
            if (!config.EnableStartSafeTime) {
                return;
            }

            GameSafeComponent gameSafe = GameSafeComponent.KEY.get(context.player().getWorld());
            if (!context.gameWorld().isRunning() || !context.aliveAndSurvival() || !gameSafe.isSafe()) {
                return;
            }

            int safeTime = config.StartingCooldown - gameSafe.safeTicks / 20 - 1;
            Text safeTimeText = safeTime <= 0
                    ? Text.translatable("tip.kinswathe.game_no_safe_time", safeTime)
                    : Text.translatable("tip.kinswathe.game_safe_time", safeTime);
            context.drawContext().drawCenteredTextWithShadow(
                    context.textRenderer(),
                    safeTimeText,
                    context.width() / 2,
                    20,
                    Color.GREEN.getRGB()
            );
        });
    }
}
